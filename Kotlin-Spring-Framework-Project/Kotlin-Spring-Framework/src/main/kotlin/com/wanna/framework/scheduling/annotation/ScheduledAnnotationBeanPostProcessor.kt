package com.wanna.framework.scheduling.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.EmbeddedValueResolverAware
import com.wanna.framework.context.aware.BeanNameAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.ContextRefreshedEvent
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.context.processor.beans.DestructionAwareBeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.core.util.StringUtils
import com.wanna.framework.scheduling.TaskScheduler
import com.wanna.framework.scheduling.config.*
import com.wanna.framework.scheduling.support.ScheduledMethodRunnable
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService

/**
 * 处理定时任务的注解(@Scheduled)的方法的BeanPostProcessor；
 * 负责扫描@Scheduled的方法，注册到ScheduledTaskRegistrar当中去进行定时调度
 *
 * @see ScheduledTaskRegistrar
 */
open class ScheduledAnnotationBeanPostProcessor : ApplicationListener<ContextRefreshedEvent>, BeanNameAware,
    ApplicationContextAware, BeanFactoryAware, EmbeddedValueResolverAware, MergedBeanDefinitionPostProcessor,
    DestructionAwareBeanPostProcessor, SmartInitializingSingleton, ScheduledTaskHolder, DisposableBean {
    companion object {
        const val DEFAULT_TASK_SCHEDULER_BEAN_NAME = "taskScheduler"  // 默认的TaskScheduler的beanName
        private val logger = LoggerFactory.getLogger(ScheduledAnnotationBeanPostProcessor::class.java)
    }

    // 定时任务的调度器，负责去进行定时任务的注册与调度
    private var registrar = ScheduledTaskRegistrar()

    // 任务调度器
    private var scheduler: Any? = null

    private var beanName: String? = null

    private var applicationContext: ApplicationContext? = null

    private var beanFactory: BeanFactory? = null

    private var embeddedValueResolver: StringValueResolver? = null

    // 没有标注@Scheduled注解的类的集合
    private var nonAnnotatedClasses = Collections.newSetFromMap<Class<*>>(ConcurrentHashMap())

    // 要去定时调度的任务列表...维护了全部的Bean上的全部@Scheduled的定时任务
    private val scheduledTasks = HashMap<Any, MutableSet<ScheduledTask>>()

    override fun afterSingletonsInstantiated() {
        this.nonAnnotatedClasses.clear()
        if (this.applicationContext == null) {
            finishRegistration()
        }
    }

    /**
     * 获取所有的ScheduledTask列表
     *
     * @return 当前已经注册的ScheduledTask列表
     */
    override fun getScheduledTasks(): Set<ScheduledTask> {
        val result = HashSet<ScheduledTask>()
        synchronized(scheduledTasks) {
            this.scheduledTasks.forEach { (_, tasks) ->
                result += tasks
            }
        }
        result += this.registrar.getScheduledTasks()
        return result
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun setBeanName(beanName: String) {
        this.beanName = beanName
    }

    override fun setEmbeddedValueResolver(resolver: StringValueResolver) {
        this.embeddedValueResolver = resolver
    }

    /**
     * 在ContextRefreshed事件发布时，需要完成Scheduler的设置，并已经处理到的启动所有的定时任务
     *
     * @param event ContextRefreshedEvent
     */
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        finishRegistration() // 完成注册
    }

    private fun finishRegistration() {
        val scheduler = this.scheduler
        val beanFactory = this.beanFactory ?: throw IllegalStateException("BeanFactory不能为空")
        if (scheduler != null) {
            this.registrar.setScheduler(scheduler)
        }

        // 尝试从BeanFactory当中获取TaskScheduler/ScheduledExecutorService设置到Registrar当中...
        initScheduler(beanFactory)

        // 完成Registrar的初始化工作，启动之前加入进去的所有的定时调度任务...
        this.registrar.afterPropertiesSet()
    }

    /**
     * 从给定的beanFactory当中去初始化Scheduler，支持去寻找TaskScheduler/ScheduledExecutorService两种类型
     *
     * @param beanFactory 要去寻找Scheduler的BeanFactory
     * @see TaskScheduler
     * @see ScheduledExecutorService
     */
    private fun initScheduler(beanFactory: BeanFactory) {
        if (this.registrar.getScheduler() == null) {
            try {
                this.registrar.setScheduler(resolveSchedulerBean(beanFactory, TaskScheduler::class.java, false))
            } catch (ex: NoUniqueBeanDefinitionException) {
                if (logger.isTraceEnabled) {
                    logger.trace("在BeanFactory当中找到了不止一个TaskScheduler，尝试根据name=[$DEFAULT_TASK_SCHEDULER_BEAN_NAME]去进行获取")
                }
                try {
                    this.registrar.setScheduler(resolveSchedulerBean(beanFactory, TaskScheduler::class.java, true))
                } catch (ex2: NoSuchBeanDefinitionException) {
                    if (logger.isInfoEnabled) {
                        logger.info("在BeanFactory当中找到了多个TaskScheduler，但是没有存在有name=[$DEFAULT_TASK_SCHEDULER_BEAN_NAME]的TaskScheduler")
                    }
                }
            } catch (ex: NoSuchBeanDefinitionException) {
                if (logger.isTraceEnabled) {
                    logger.trace("在BeanFactory当中没有找到TaskScheduler，尝试去寻找ScheduledExecutorService")
                }
                try {
                    this.registrar.setScheduler(
                        resolveSchedulerBean(
                            beanFactory, ScheduledExecutorService::class.java, false
                        )
                    )
                } catch (ex2: NoUniqueBeanDefinitionException) {
                    if (logger.isTraceEnabled) {
                        logger.trace("在BeanFactory当中存在有多个ScheduledExecutor，尝试根据name=[$DEFAULT_TASK_SCHEDULER_BEAN_NAME]去进行获取")
                    }
                    try {
                        this.registrar.setScheduler(
                            this.registrar.setScheduler(
                                resolveSchedulerBean(
                                    beanFactory, ScheduledExecutorService::class.java, true
                                )
                            )
                        )
                    } catch (ex3: NoSuchBeanDefinitionException) {
                        if (logger.isInfoEnabled) {
                            logger.info("在BeanFactory当中找到了多个ScheduledExecutorService，但是没有存在有name=[$DEFAULT_TASK_SCHEDULER_BEAN_NAME]的TaskScheduler")
                        }
                        // give up
                    }
                } catch (ex: NoSuchBeanDefinitionException) {
                    // give up
                    logger.info("无法从BeanFactory当寻找到合适的TaskScheduler/ScheduledExecutorService去执行定时任务")
                }
            }
        }
    }

    /**
     * 从给定的beanFactory当中去解析SchedulerBean
     *
     * @param beanFactory beanFactory
     * @param schedulerType SchedulerType
     * @param byName 是要按照name去进行解析吗？
     */
    private fun <T> resolveSchedulerBean(
        beanFactory: BeanFactory, schedulerType: Class<T>, byName: Boolean
    ): T {
        return if (byName) {
            beanFactory.getBean(DEFAULT_TASK_SCHEDULER_BEAN_NAME, schedulerType)
        } else {
            beanFactory.getBean(schedulerType)
        }
    }

    /**
     * 在完成Bean的初始化之后，需要扫描beanClass上的@Scheduled注解，将它注册为定时任务
     *
     * @param bean bean
     * @param beanName beanName
     */
    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        val clazz = bean::class.java
        val annotatedMethods = LinkedHashMap<Method, Set<Scheduled>>()

        // 如果之前已经确定为没有标注@Scheduled的类，那么直接pass掉
        if (nonAnnotatedClasses.contains(clazz)) {
            return bean
        }

        // 扫描所有的@Scheduled注解，注册为定时任务
        ReflectionUtils.doWithMethods(clazz) {
            if (AnnotatedElementUtils.hasAnnotation(it, Scheduled::class.java)) {
                annotatedMethods[it] = AnnotatedElementUtils.getAllMergedAnnotations(it, Scheduled::class.java).toSet()
            }
        }
        if (annotatedMethods.isEmpty()) {
            nonAnnotatedClasses.add(clazz)
        } else {
            annotatedMethods.forEach { (method, annotations) ->
                annotations.forEach { ann -> processScheduled(ann, bean, method) }
            }
            if (logger.isTraceEnabled) {
                logger.trace("在beanClass=[$clazz]上找到了[${annotatedMethods.size}]个@Scheduled的方法")
            }
        }
        return bean
    }

    /**
     * 处理一个@Scheduled注解上面配置的各个属性，往ScheduledTaskRegistrar当中去添加定时任务
     *
     * * 1.处理固定速率的任务
     * * 2.处理固定延时的任务
     *
     * @param scheduled 寻找到的@Scheduled注解
     * @param bean bean object
     * @param method 要执行的目标方法
     */
    protected open fun processScheduled(scheduled: Scheduled, bean: Any, method: Method) {
        // 是否从@Scheduled注解上去寻找到了定时任务的配置？因为没有找到的话，需要丢出异常
        var processedSchedule = true

        val runnable = createRunnable(bean, method)
        // ScheduledTask列表
        val tasks = HashSet<ScheduledTask>()
        val embeddedValueResolver = this.embeddedValueResolver

        // 解析initialDelay(不支持同时从initialDelay和initialDelayString两种方式去进行配置，只能配置其中一个)
        // 如果使用String的方式去给定的话，那么可以支持使用占位符解析的方式去进行解析
        var initialDelay = scheduled.initialDelay
        val initialDelayString = scheduled.initialDelayString
        if (StringUtils.hasText(initialDelayString)) {
            if (initialDelay != -1L) throw IllegalStateException("不支持在@Scheduled注解上去同时去配置initialDelay和initialDelayString属性")
            initialDelay = if (embeddedValueResolver != null) {
                embeddedValueResolver.resolveStringValue(initialDelayString)?.toLong()
                    ?: throw IllegalStateException("无法从@Scheduled注解上解析到initialDelay属性")
            } else {
                initialDelayString.toLong()
            }
        }

        // 如果initialDelay为负数的话，说明它没有被初始化过，那么设置为0
        initialDelay = if (initialDelay < 0) 0 else initialDelay

        // 添加固定速率的任务到ScheduledTaskRegistrar
        // 如果同时配置了fixedRate和fixedRateString的话，那么它们将会被分别注册成为两个定时任务
        var fixedRate = scheduled.fixedRate
        val fixedRateString = scheduled.fixedRateString
        if (fixedRate >= 0) {
            processedSchedule = true
            tasks.add(this.registrar.scheduleFixedRateTask(FixedRateTask(runnable, fixedRate, initialDelay)))
        }
        if (StringUtils.hasText(fixedRateString)) {
            fixedRate = if (embeddedValueResolver != null) {
                embeddedValueResolver.resolveStringValue(fixedRateString)?.toLong()
                    ?: throw IllegalStateException("无法从@Scheduled注解上解析到fixedRate属性")
            } else {
                fixedRateString.toLong()
            }
            processedSchedule = true
            tasks.add(this.registrar.scheduleFixedRateTask(FixedRateTask(runnable, fixedRate, initialDelay)))
        }

        // 添加固定延时的任务到ScheduledTaskRegistrar
        // 如果同时配置了fixedDelay和fixedDelayString的话，那么它们将会被分别注册成为两个定时任务
        var fixedDelay = scheduled.fixedDelay
        val fixedDelayString = scheduled.fixedDelayString
        if (fixedDelay >= 0) {
            processedSchedule = true
            tasks.add(this.registrar.scheduleFixedDelayTask(FixedDelayTask(runnable, fixedDelay, initialDelay)))
        }
        if (StringUtils.hasText(fixedDelayString)) {
            fixedDelay = if (embeddedValueResolver != null) {
                embeddedValueResolver.resolveStringValue(fixedDelayString)?.toLong()
                    ?: throw IllegalStateException("无法从@Scheduled注解上解析到fixedDelay属性")
            } else {
                fixedRateString.toLong()
            }
            processedSchedule = true
            tasks.add(this.registrar.scheduleFixedDelayTask(FixedRateTask(runnable, fixedDelay, initialDelay)))
        }
        if (!processedSchedule) {
            throw IllegalStateException("没有从@Scheduled上找到合适的定时任务的配置，支持的定时任务类型包括fixedRate/fixedDelay/cron等类型")
        }

        // 添加Task列表...
        synchronized(this.scheduledTasks) {
            this.scheduledTasks.putIfAbsent(method, LinkedHashSet(4))
            this.scheduledTasks[method]?.addAll(tasks)
        }
    }

    /**
     * 为@Scheduled方法去创建合适的Runnable，将"run"方法设置为反射执行给定的方法
     *
     * @param bean bean object
     * @param method bean method
     * @return 创建好的ScheduledMethod的Runnable
     */
    protected open fun createRunnable(bean: Any, method: Method): Runnable = ScheduledMethodRunnable(bean, method)

    /**
     * 针对某个Bean去处理destroy，需要去进行destroy时，将该Bean当中的所有的ScheduledTask去进行cancel掉
     *
     * @param bean 要去进行destroy的Bean
     * @param beanName beanName
     */
    override fun postProcessBeforeDestruction(bean: Any, beanName: String) {
        var tasks: MutableSet<ScheduledTask>?
        synchronized(this.scheduledTasks) {
            tasks = this.scheduledTasks.remove(bean)
        }
        tasks?.forEach(ScheduledTask::cancel)  // cancel
        tasks?.clear()  // clear
    }

    /**
     * 是否需要为指定的Bean去注册destruction的回调？
     *
     * @param bean 要去注册回调的Bean
     * @return 如果该Bean有@Scheduled方法，那么return true，否则return false
     */
    override fun requiresDestruction(bean: Any): Boolean {
        synchronized(this.scheduledTasks) {
            return this.scheduledTasks.containsKey(bean)
        }
    }

    override fun destroy() {
        // 1.关闭当前维护的所有的ScheduledTasks列表
        synchronized(this.scheduledTasks) {
            this.scheduledTasks.map { it.value }.flatMap { it.toList() }.forEach(ScheduledTask::cancel)
            this.scheduledTasks.clear()  // clear
        }
        // 2.关闭Registrar当中的所有的ScheduledTask，并关闭线程池
        this.registrar.destroy()
    }
}