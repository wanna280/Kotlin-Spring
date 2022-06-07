package com.wanna.framework.scheduling.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.factory.BeanFactory
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
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.core.util.StringUtils
import com.wanna.framework.scheduling.TaskScheduler
import com.wanna.framework.scheduling.config.FixedDelayTask
import com.wanna.framework.scheduling.config.FixedRateTask
import com.wanna.framework.scheduling.config.ScheduledTask
import com.wanna.framework.scheduling.config.ScheduledTaskRegistrar
import com.wanna.framework.scheduling.support.ScheduledMethodRunnable
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils
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
    DestructionAwareBeanPostProcessor, SmartInitializingSingleton {
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

    private fun <T> resolveSchedulerBean(
        beanFactory: BeanFactory, schedulerType: Class<T>, byName: Boolean
    ): T {
        if (byName) {
            return beanFactory.getBean(DEFAULT_TASK_SCHEDULER_BEAN_NAME, schedulerType)
        } else {
            return beanFactory.getBean(schedulerType)
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
     * @param scheduled @Scheduled注解
     * @param bean bean
     * @param method 要执行的目标方法
     */
    protected open fun processScheduled(scheduled: Scheduled, bean: Any, method: Method) {
        val runnable = createRunnable(bean, method)
        // ScheduledTask列表
        val tasks = HashSet<ScheduledTask>()
        val embeddedValueResolver = this.embeddedValueResolver
        var initialDelay = scheduled.initialDelay
        if (initialDelay < 0) {
            initialDelay = 0
        }

        // 添加固定速率的任务到ScheduledTaskRegistrar
        var fixedRate = scheduled.fixedRate
        val fixedRateString = scheduled.fixedRateString
        if (fixedRate >= 0) {
            tasks.add(this.registrar.scheduleFixedRateTask(FixedRateTask(runnable, fixedRate, initialDelay)))
        }
        if (StringUtils.hasText(fixedRateString)) {
            fixedRate = if (embeddedValueResolver != null) {
                embeddedValueResolver.resolveStringValue(fixedRateString)?.toLong()
                    ?: throw IllegalStateException("无法从@Scheduled注解上解析到fixedRate")
            } else {
                fixedRateString.toLong()
            }
            tasks.add(this.registrar.scheduleFixedRateTask(FixedRateTask(runnable, fixedRate, initialDelay)))
        }

        // 添加固定延时的任务到ScheduledTaskRegistrar
        var fixedDelay = scheduled.fixedDelay
        val fixedDelayString = scheduled.fixedDelayString
        if (fixedDelay >= 0) {
            tasks.add(this.registrar.scheduleFixedDelayTask(FixedDelayTask(runnable, fixedDelay, initialDelay)))
        }
        if (StringUtils.hasText(fixedDelayString)) {
            fixedDelay = if (embeddedValueResolver != null) {
                embeddedValueResolver.resolveStringValue(fixedDelayString)?.toLong()
                    ?: throw IllegalStateException("无法从@Scheduled注解上解析到fixedDelay")
            } else {
                fixedRateString.toLong()
            }
            tasks.add(this.registrar.scheduleFixedDelayTask(FixedRateTask(runnable, fixedDelay, initialDelay)))
        }

        // 添加Task列表...
        synchronized(this.scheduledTasks) {
            this.scheduledTasks.putIfAbsent(method, LinkedHashSet(4))
            this.scheduledTasks[method]?.addAll(tasks)
        }
    }

    protected open fun createRunnable(bean: Any, method: Method): Runnable {
        return ScheduledMethodRunnable(bean, method)
    }
}