package com.wanna.framework.context

import com.wanna.framework.context.ConfigurableApplicationContext.Companion.CONVERSION_SERVICE_BEAN_NAME
import com.wanna.framework.context.event.*
import com.wanna.framework.context.exception.BeansException
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationContextAwareProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationListenerDetector
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.context.support.DefaultLifecycleProcessor
import com.wanna.framework.context.support.PostProcessorRegistrationDelegate
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.StandardEnvironment


/**
 * 提供了一个抽象的ApplicationContext的实现
 */
abstract class AbstractApplicationContext : ConfigurableApplicationContext {

    // ApplicationContext的环境信息
    private var environment: ConfigurableEnvironment? = null

    // 存放BeanFactoryPostProcessor的列表
    private val beanFactoryPostProcessors: MutableList<BeanFactoryPostProcessor> = ArrayList()

    // 存放监听器列表
    private var applicationListeners: MutableSet<ApplicationListener<*>> = LinkedHashSet()

    // 事件多拨器，完成事件的发布，回调所有的监听器
    private var applicationEventMulticaster: ApplicationEventMulticaster? = null

    companion object {
        const val APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster"  // 事件多拨器的beanName
        const val LIFECYCLE_PROCESSOR_BEAN_NAME = "lifeProcessor";  // LifeCycleProcessor的beanName
    }

    // 这是容器中的早期事件，有可能发布事件时，容器的多拨器还没完成初始化，不能完成发布，因此就需要延时去进行发布
    private var earlyApplicationEvents: MutableSet<ApplicationEvent>? = null

    // 早期的监听器
    private var earlyApplicationListeners: MutableSet<ApplicationListener<*>>? = null

    // 应用的启动大锁，只要拿到这个锁，才能去对容器去进行启动或者关闭
    private val startupShutdownMonitor = Any()

    // 生命周期处理器
    private var lifecycleProcessor: LifecycleProcessor? = null

    override fun refresh() {
        synchronized(this.startupShutdownMonitor) {
            // 准备完成容器的刷新工作，创建好早期监听器和早期事件列表、初始化PropertySources
            prepareRefresh()

            // 通知子类去获取一个新鲜的BeanFactory，并告诉子类去完成内部(internal)的BeanFactory的刷新
            val beanFactory = obtainFreshBeanFactory()

            // 完成BeanFactory的初始化，为BeanFactory当中注册一些需要使用到的依赖
            prepareBeanFactory(beanFactory)

            try {
                // BeanFactory的后置处理工作，是一个钩子方法，交给子类去进行完成
                postProcessBeanFactory(beanFactory)

                // 执行已经注册到ApplicationContext当中的所有的BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory)

                // 把到容器当中的BeanPostProcessor的BeanDefinition去完成实例化，并注册到beanFactory当中
                registerBeanPostProcessors(beanFactory)

                // 初始化事件多播器，如果容器当中有合适的ApplicationEventMulticaster的话，那么使用自定义的；
                // 不然采用默认的SimpleApplicationEventMulticaster作为事件多拨器并注册到beanFactory当中
                initApplicationEventMulticaster(beanFactory)

                // 初始化容器当中需要用到的MessageSource，主要是和国际化相关的，这里没提供相关的实现...
                initMessageSource()

                // 交给子类去完成，子类可以在这里去完成TomcatWebServer等对象的创建...
                onRefresh()

                // 注册监听器，在这个步骤之前所发布的事件，都会被保存到早期事件当中...这里会去完成所有早期事件的发布
                // 在这里完成了监听器的注册之后，ApplicationEventMulticaster已经可以处理事件了，就不需要早期事件列表的存在了
                registerListeners()

                // 完成剩下的所有单实例Bean的初始化工作
                finishBeanFactoryInitialization(beanFactory)

                // 完成容器的刷新
                finishRefresh()
            } catch (ex: BeansException) {
                throw ex
            }
        }
    }

    /**
     * 初始化PropertySources，交给子类去进行完成初始化，是一个模板方法，在容器启动时，会自动完成初始化工作
     */
    protected open fun initPropertySources() {

    }

    /**
     * 准备完成容器的刷新工作
     * (1)初始化PropertySources
     * (2)完成早期事件、早期监听器的初始化工作
     */
    protected open fun prepareRefresh() {

        // 完成PropertySources的初始化工作，给环境当中注册PropertySource，可以用作配置文件的加载
        // 一般用来加载Servlet相关的配置信息到容器当中
        initPropertySources()

        // 如果earlyApplicationListener为空的话，需要初始化；如果不为空的话，需要全部转移到ApplicationListener当中
        if (this.earlyApplicationListeners == null) {
            this.applicationListeners = LinkedHashSet()
        } else {
            this.applicationListeners.clear()
            this.applicationListeners += earlyApplicationListeners!!
        }
        // 创建早期的ApplicationEvent列表
        this.earlyApplicationEvents = LinkedHashSet()
    }

    /**
     * 通知子类去完成BeanFactory的创建，通过这个方法，可以获取到一个新鲜的BeanFactory，去作为整个ApplicationContext的BeanFactory
     *
     * @see GenericApplicationContext.obtainFreshBeanFactory
     */
    protected open fun obtainFreshBeanFactory(): ConfigurableListableBeanFactory {
        refreshBeanFactory()  // refresh
        return getBeanFactory()  // getBeanFactory
    }

    /**
     * 告诉子类去创建BeanFactory，子类必须在refreshBeanFactory或者之前，创建好了BeanFactory；
     * 这个方法会在ApplicationContext的刷新之前就会去进行自动回调
     * 如果对ApplicationContext重复调用这个方法，有可能会产生IllegalStateException；
     *
     * @see refresh
     */
    protected abstract fun refreshBeanFactory()

    /**
     * 在完成BeanFactory的刷新之后，应该为当前的ApplicationContext提供getBeanFactory方法，去获取BeanFactory
     */
    abstract override fun getBeanFactory(): ConfigurableListableBeanFactory

    /**
     * 完成BeanFactoryPostProcessor的执行
     */
    protected open fun invokeBeanFactoryPostProcessors(beanFactory: ConfigurableListableBeanFactory) {
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, beanFactoryPostProcessors)
    }

    /**
     * 完成所有的BeanPostProcessor的注册工作
     */
    protected open fun registerBeanPostProcessors(beanFactory: ConfigurableListableBeanFactory) {
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this)
    }

    /**
     * 完成MessageSource的初始化，和国际化相关
     */
    protected open fun initMessageSource() {

    }

    /**
     * 完成事件多拨器的初始化，如果容器中已经有了，那么使用容器中的作为要使用的，如果容器中没有，那么将会采用默认的事件多拨器
     *
     * @see refresh
     * @see ApplicationEventMulticaster
     * @see SimpleApplicationEventMulticaster
     */
    protected open fun initApplicationEventMulticaster(beanFactory: ConfigurableListableBeanFactory) {
        // 如果容器中已经注册了ApplicationEventMulticaster，那么采用自定义的
        if (beanFactory.containsSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            this.applicationEventMulticaster =
                beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster::class.java)

            // 如果容器当中没有注册，那么就使用默认的，并把默认的注册到BeanFactory当中，后续就可以去完成Autowire
        } else {
            this.applicationEventMulticaster = SimpleApplicationEventMulticaster()
            beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster!!)
        }
    }

    /**
     * 完成BeanFactory的准备工作，给BeanFactory当中添加一些相关的依赖
     */
    protected open fun prepareBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 给容器中注册可以被解析的依赖，包括BeanFactory，Application，ApplicationEventPublisher等
        beanFactory.registerResolvableDependency(BeanFactory::class.java, beanFactory)
        beanFactory.registerResolvableDependency(ApplicationContext::class.java, this)
        beanFactory.registerResolvableDependency(ApplicationEventPublisher::class.java, this)

        // 添加ApplicationContext的BeanPostProcessor，完成BeanClassLoaderAware/EnvironmentAware等Aware接口的处理
        this.addBeanPostProcessor(ApplicationContextAwareProcessor(this))

        // 添加ApplicationListener的Detector，完成EventListener的探测和注册
        this.addBeanPostProcessor(ApplicationListenerDetector(this))
    }

    /**
     * 对BeanFactory完成后置处理工作
     */
    protected open fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }

    /**
     * 钩子方法，交给子类去完成
     */
    protected open fun onRefresh() {

    }

    /**
     * 完成容器刷新之后需要完成的处理工作
     */
    protected open fun finishRefresh() {

        // 初始化生命周期处理器
        initLifecycleProcessor()

        // 完成注册的所有Lifecycle的刷新工作
        getLifecycleProcessor().onRefresh()

        // 发布ApplicationContext已经完成刷新的事件
        publishEvent(ContextRefreshedEvent(this))
    }

    /**
     * 初始化生命周期的处理器，如果容器中已经有了生命周期处理器，不然使用默认的生命周期处理器
     */
    protected open fun initLifecycleProcessor() {
        if (containsBeanDefinition(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
            this.lifecycleProcessor = getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor::class.java)
        } else {
            val defaultLifecycleProcessor = DefaultLifecycleProcessor()
            defaultLifecycleProcessor.setBeanFactory(this.getBeanFactory())
            this.lifecycleProcessor = defaultLifecycleProcessor
            getBeanFactory().registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, defaultLifecycleProcessor)
        }
    }

    /**
     * (1)完成所有的监听器的注册，将容器当中的ApplicationListener，全部转移到ApplicationEventMulticaster当中
     * (2)将这之前的所有发布的早期事件，使用ApplicationEventMulticaster去全部进行发布...
     */
    protected open fun registerListeners() {
        // 将ApplicationListener注册到ApplicationEventMulticaster当中
        applicationListeners.forEach { getApplicationEventMulticaster().addApplicationListener(it) }

        // 将之前已经注册到容器当中的ApplicationListener注册到ApplicationEventMulticaster当中
        val listerNames = getBeanNamesForType(ApplicationListener::class.java)
        listerNames.forEach { getApplicationEventMulticaster().addApplicationListenerBean(it) }

        val earlyApplicationEventsToProcess = this.earlyApplicationEvents!!

        // 注册完监听器之后，容器的ApplicationEventMulticaster已经初始化完成了，需要将earlyApplicationEvents设置为空
        // 不然后续过程中，发布事件时会把事件加入到早期事件列表当中去...不能让该事件被成功通过ApplicationEventMulticaster去发布
        this.earlyApplicationEvents = null


        // 发布所有的早期事件，以后发布的事件可以直接使用ApplicationEventMulticaster了，就不必使用早期事件了...
        earlyApplicationEventsToProcess.forEach { getApplicationEventMulticaster().multicastEvent(it) }
    }

    /**
     * 完成BeanFactory的初始化，对容器当中剩下的所有未进行实例化的Bean去进行该Bean的实例化和初始化工作
     */
    protected open fun finishBeanFactoryInitialization(beanFactory: ConfigurableListableBeanFactory) {
        // 如果容器当中存在了ConversionService的BeanDefinition，那么提前getBean，并设置到beanFactory的ConversionService当中
        if (containsBeanDefinition(CONVERSION_SERVICE_BEAN_NAME) && beanFactory.isTypeMatch(
                CONVERSION_SERVICE_BEAN_NAME, ConversionService::class.java
            )
        ) {
            val conversionService = beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService::class.java)
            beanFactory.setConversionService(conversionService)
        }

        // 如果容器当中没有嵌入式的值解析器，那么需要往容器当中加入一个默认的
        if (!getBeanFactory().hasEmbeddedValueResolver()) {
            getBeanFactory().addEmbeddedValueResolver(object : StringValueResolver {
                override fun resolveStringValue(strVal: String) = getEnvironment().resolveRequiredPlaceholders(strVal)
            })
        }

        // 完成剩下的所有单实例Bean的实例化和初始化工作
        beanFactory.preInstantiateSingletons()
    }

    /**
     * 添加BeanFactoryPostProcessor
     */
    override fun addBeanFactoryPostProcessor(processor: BeanFactoryPostProcessor) {
        beanFactoryPostProcessors += processor
    }

    /**
     * 添加ApplicationListener到容器当中；
     * (1)如果ApplicationEventMulticaster还没完成初始化，那么加入到ApplicationContext当中
     * (2)如果ApplicationEventMulticaster已经完成初始化，那么直接把它加入到ApplicationEventMulticaster当中
     */
    open fun addApplicationListener(listener: ApplicationListener<*>) {
        if (this.applicationEventMulticaster == null) {
            this.applicationListeners += listener
        } else {
            getApplicationEventMulticaster().addApplicationListener(listener)
        }
    }

    override fun publishEvent(event: Any) {
        val applicationEvent: ApplicationEvent?
        if (event is ApplicationEvent) {
            applicationEvent = event
        } else {
            applicationEvent = PayloadApplicationEvent(this, event)
        }

        // 如果早期事件不为空，那么加入到早期事件列表当中(此时事件多拨器还没准备好，就需要一个容器去保存早期的事件)
        // 等到ApplicationEventMulticaster已经准备好了，那么就可以使用ApplicationEventMulticaster去完成事件的发布了
        if (earlyApplicationEvents != null) {
            earlyApplicationEvents!! += applicationEvent
        } else {
            getApplicationEventMulticaster().multicastEvent(applicationEvent)
        }
    }

    open fun getApplicationEventMulticaster(): ApplicationEventMulticaster {
        if (this.applicationEventMulticaster == null) {
            throw IllegalStateException("ApplicationContext还没完成初始化，无法获取到ApplicationEventMulticaster")
        }
        return this.applicationEventMulticaster!!
    }

    override fun setEnvironment(environment: ConfigurableEnvironment) {
        this.environment = environment
    }

    override fun getEnvironment(): ConfigurableEnvironment {
        if (this.environment == null) {
            this.environment = createEnvironment()
        }
        return this.environment!!
    }

    /**
     * 创建Environment
     */
    protected open fun createEnvironment(): ConfigurableEnvironment = StandardEnvironment()

    override fun getBean(beanName: String) = getBeanFactory().getBean(beanName)

    override fun <T> getBean(beanName: String, type: Class<T>): T? = getBeanFactory().getBean(beanName, type)

    override fun <T> getBean(type: Class<T>) = getBeanFactory().getBean(type)

    override fun isSingleton(beanName: String) = getBeanFactory().isSingleton(beanName)

    override fun isPrototype(beanName: String) = getBeanFactory().isPrototype(beanName)

    override fun addBeanPostProcessor(processor: BeanPostProcessor) = getBeanFactory().addBeanPostProcessor(processor)

    override fun removeBeanPostProcessor(type: Class<*>) = getBeanFactory().removeBeanPostProcessor(type)

    override fun removeBeanPostProcessor(index: Int) = getBeanFactory().removeBeanPostProcessor(index)

    override fun isFactoryBean(beanName: String) = getBeanFactory().isFactoryBean(beanName)

    override fun isTypeMatch(beanName: String, type: Class<*>) = getBeanFactory().isTypeMatch(beanName, type)

    override fun getType(beanName: String) = getBeanFactory().getType(beanName)

    override fun getBeanNamesForType(type: Class<*>) = getBeanFactory().getBeanNamesForType(type)

    override fun <T> getBeansForType(type: Class<T>) = getBeanFactory().getBeansForType(type)

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>) =
        getBeanFactory().getBeanNamesForTypeIncludingAncestors(type)

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>) =
        getBeanFactory().getBeansForTypeIncludingAncestors(type)

    open fun getBeanFactoryPostProcessors(): List<BeanFactoryPostProcessor> = this.beanFactoryPostProcessors

    open fun getLifecycleProcessor(): LifecycleProcessor = this.lifecycleProcessor!!

}