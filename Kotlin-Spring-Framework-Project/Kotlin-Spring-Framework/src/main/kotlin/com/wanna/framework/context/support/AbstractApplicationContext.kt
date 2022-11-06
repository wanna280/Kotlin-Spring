package com.wanna.framework.context.support

import com.wanna.framework.beans.BeansException
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.APPLICATION_STARTUP_BEAN_NAME
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.CONVERSION_SERVICE_BEAN_NAME
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.ENVIRONMENT_BEAN_NAME
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.LOAD_TIME_WEAVER_BEAN_NAME
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.SYSTEM_ENVIRONMENT_BEAN_NAME
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.SYSTEM_PROPERTIES_BEAN_NAME
import com.wanna.framework.context.LifecycleProcessor
import com.wanna.framework.context.event.*
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.context.processor.beans.internal.ApplicationContextAwareProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationListenerDetector
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.context.weaving.LoadTimeWeaverAwareProcessor
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.PathMatchingResourcePatternResolver
import com.wanna.framework.core.io.support.ResourcePatternResolver
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.lang.Nullable
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 提供了一个抽象的ApplicationContext的实现
 * (1)内部集成了Environment，可以提供属性值(配置文件、系统属性、系统环境等)的来源
 * (2)内部集成了BeanFactoryPostProcessor，可以对BeanFactory去进行一些处理工作
 * (3)内部集成了ApplicationEventMulticaster，可以实现对事件(ApplicationEvent)进行发布
 * (4)内部集成了ResourceLoader，可以提供资源的加载
 *
 * @see ApplicationEventMulticaster
 * @see ApplicationContext
 * @see ConfigurableEnvironment
 * @see StandardEnvironment
 * @see BeanFactoryPostProcessor
 * @see DefaultResourceLoader
 */
abstract class AbstractApplicationContext : ConfigurableApplicationContext, DefaultResourceLoader() {
    companion object {

        /**
         * Spring的EventMulticaster事件多拨器的beanName
         */
        const val APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster"

        /**
         * Spring的LifeCycleProcessor的beanName
         */
        const val LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor"

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AbstractApplicationContext::class.java)
    }

    /**
     * ApplicationContext的环境信息，保存了配置文件、系统环境、系统属性等信息
     */
    private var environment: ConfigurableEnvironment? = null

    /**
     * 父ApplicationContext(可以为null)
     */
    @Nullable
    private var parent: ApplicationContext? = null

    /**
     * ResourcePatternResolver，本身也是一个ResourceLoader，提供资源的解析和加载；
     * 因为ApplicationContext本身也是一个ResourcePatternResolver，而且继承了DefaultResourceLoader，
     * 因此就拥有了getResource方法，但是getResources方法并没有，因此我们需要组合一个ResourcePatternResolver，
     * 用于去提供getResources
     *
     * @see ResourcePatternResolver
     * @see DefaultResourceLoader
     * @see getResources
     * @see getResource
     */
    private val resourcePatternResolver: ResourcePatternResolver = this.getResourcePatternResolver()

    /**
     * 存放BeanFactoryPostProcessor的列表，支持对beanFactory去进行后置处理工作
     */
    private val beanFactoryPostProcessors = ArrayList<BeanFactoryPostProcessor>()

    /**
     * 存放监听器列表
     */
    private var applicationListeners = LinkedHashSet<ApplicationListener<*>>()

    /**
     * 事件多拨器，完成事件的发布，回调所有的监听器
     */
    private var applicationEventMulticaster: ApplicationEventMulticaster? = null

    /**
     * 这是容器中的早期事件，有可能发布事件时，容器的多拨器还没完成初始化，不能完成发布，因此就需要延时去进行发布
     * 但是发布的事件并不能直接扔掉，应该进行保存，这个列表存放的就是早期发布的事件列表
     */
    private var earlyApplicationEvents: MutableSet<ApplicationEvent>? = null

    /**
     * 早期的监听器列表
     */
    private var earlyApplicationListeners: MutableSet<ApplicationListener<*>>? = null

    /**
     * ApplicationContext的应用的启动大锁，只要拿到这个锁，才能去对容器去进行启动或者关闭
     */
    private val startupShutdownMonitor = Any()

    /**
     * 生命周期处理器，负责回调所有的LifecycleBean(比如WebServer)
     */
    @Nullable
    private var lifecycleProcessor: LifecycleProcessor? = null

    /**
     * ApplicationStartup，记录Spring应用启动过程当中的步骤信息
     */
    private var applicationStartup: ApplicationStartup = ApplicationStartup.DEFAULT

    /**
     *  Shutdown的回调钩子
     */
    @Nullable
    private var shutdownHook: Thread? = null

    /**
     * 当前的ApplicationContext是否还活跃？(当关闭时会被设置为true)
     */
    private var active = AtomicBoolean(true)

    /**
     * 当前的ApplicationContext是否已经关闭了？
     */
    private var closed = AtomicBoolean(false)

    /**
     * 为当前的ApplicationContext去生成一个id
     *
     * @return 当前的ApplicationContext的id
     */
    override fun getId(): String = javaClass.name + "@" + System.identityHashCode(this).toString(16)

    /**
     * 获取ResolvePatternResolver，提供资源的解析，支持子类当中去进行自定义
     *
     * @return ResourcePatternResolver
     */
    protected open fun getResourcePatternResolver(): ResourcePatternResolver = PathMatchingResourcePatternResolver(this)

    /**
     * 完成当前ApplicationContext的刷新工作，引导Spring BeanFactory的启动
     */
    override fun refresh() {
        synchronized(this.startupShutdownMonitor) {
            val contextRefresh = this.applicationStartup.start("spring.context.refresh") // start context refresh
            // 准备完成容器的刷新工作，创建好早期监听器和早期事件列表、初始化PropertySources
            prepareRefresh()

            // 通知子类去获取一个新鲜的BeanFactory，并告诉子类去完成内部(internal)的BeanFactory的刷新
            val beanFactory = obtainFreshBeanFactory()

            // 完成BeanFactory的初始化，为BeanFactory当中注册一些需要使用到的依赖
            prepareBeanFactory(beanFactory)

            try {
                // BeanFactory的后置处理工作，是一个钩子方法，交给子类去进行完成
                postProcessBeanFactory(beanFactory)

                val postProcess =
                    this.applicationStartup.start("spring.context.beans.post-process")  // start post process
                // 执行已经注册到ApplicationContext当中的所有的BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(beanFactory)

                // 把到容器当中的BeanPostProcessor的BeanDefinition去完成实例化，并注册到beanFactory当中
                registerBeanPostProcessors(beanFactory)
                postProcess.end()  // end postProcess

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
                if (logger.isDebugEnabled) {
                    logger.debug("在Spring ApplicationContext刷新过程当中遇到异常", ex)
                }
                this.destroyBeans()
                throw ex
            } finally {
                contextRefresh.end()  // end context refresh
            }
        }
    }

    /**
     * 初始化PropertySources，交给子类去进行完成初始化；
     * 是一个模板方法，在容器启动时会自动回调，去完成初始化工作
     *
     * @see com.wanna.framework.core.environment.PropertySource
     * @see com.wanna.framework.core.environment.MutablePropertySources
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

        // 如果earlyApplicationListeners为空的话，需要初始化earlyApplicationListeners，并将所有的ApplicationListener转移到里面去
        // 如果earlyApplicationListeners不为空的话，需要把earlyApplicationListeners中的ApplicationListener转移到applicationListeners中
        // 最终实现的效果是：earlyApplicationListeners和applicationListeners中的ApplicationListener**完全一致**
        if (this.earlyApplicationListeners == null) {
            this.earlyApplicationListeners = LinkedHashSet(this.applicationListeners)
        } else {
            this.applicationListeners.clear()
            this.applicationListeners += earlyApplicationListeners!!
        }
        // 创建早期的ApplicationEvent列表
        this.earlyApplicationEvents = LinkedHashSet()
    }

    /**
     * 通知子类去完成BeanFactory的创建，通过这个方法可以获取到一个新鲜的BeanFactory，去作为整个ApplicationContext的BeanFactory
     *
     * @see GenericApplicationContext.obtainFreshBeanFactory
     * @return ConfigurableListableBeanFactory
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
     * 在完成BeanFactory的刷新之后，应该为当前的ApplicationContext提供getBeanFactory方法，去获取BeanFactory；
     * SpringApplication的ApplicationContext的刷新过程当中，需要BeanFactory才能进行
     *
     * @return ConfigurableListableBeanFactory
     */
    abstract override fun getBeanFactory(): ConfigurableListableBeanFactory

    /**
     * 完成BeanFactoryPostProcessor的执行，完成BeanDefinition的加载以及BeanFactory的后置处理工作
     *
     * @param beanFactory beanFactory
     */
    protected open fun invokeBeanFactoryPostProcessors(beanFactory: ConfigurableListableBeanFactory) {
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, beanFactoryPostProcessors)

        // 因为完成了BeanFactoryPostProcessor的执行，因此可能还会往容器当中注册一些Bean，其中就可能包含LoadTimeWeaver的Bean
        // 因此这里还需要去进行一次检测，如果容器当中包含了LoadTimeWeaver的Bean，那么这里需要添加LoadTimeWeaverAware的处理器
        if (containsBeanDefinition(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(LoadTimeWeaverAwareProcessor(beanFactory))
        }
    }

    /**
     * 完成所有的BeanPostProcessor的注册工作，拿出容器中所有类型为BeanPostProcessor的Bean，完成实例化并注册到beanFactory当中
     *
     * @param beanFactory beanFactory
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
     *
     * @param beanFactory BeanFactory
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
     *
     * @param beanFactory BeanFactory
     */
    protected open fun prepareBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 给容器中注册可以被解析的依赖，包括BeanFactory，Application，ApplicationEventPublisher等，支持去进行Autowire
        beanFactory.registerResolvableDependency(BeanFactory::class.java, beanFactory)
        beanFactory.registerResolvableDependency(ApplicationContext::class.java, this)
        beanFactory.registerResolvableDependency(ApplicationEventPublisher::class.java, this)
        beanFactory.registerResolvableDependency(ResourceLoader::class.java, this)

        // 添加ApplicationContext的BeanPostProcessor，完成BeanClassLoaderAware/EnvironmentAware等Aware接口的处理
        beanFactory.addBeanPostProcessor(ApplicationContextAwareProcessor(this))

        // 添加ApplicationListener的Detector，完成EventListener的探测和注册
        beanFactory.addBeanPostProcessor(ApplicationListenerDetector(this))

        // 如果容器当中包含了LoadTimeWeaver的Bean，那么需要添加LoadTimeWeaverAware的处理器
        if (containsBeanDefinition(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(LoadTimeWeaverAwareProcessor(beanFactory))
        }

        // 注册ApplicationContext的环境对象到beanFactory当中
        if (!containsBeanDefinition(ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment())
        }
        // 注册系统属性对象到beanFactory当中
        if (!containsBeanDefinition(SYSTEM_PROPERTIES_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties())
        }
        // 注册系统环境对象到beanFactory当中
        if (!containsBeanDefinition(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment())
        }
        // 把ApplicationStartup对象设置到beanFactory当中
        if (!containsBeanDefinition(APPLICATION_STARTUP_BEAN_NAME)) {
            beanFactory.registerSingleton(APPLICATION_STARTUP_BEAN_NAME, getApplicationStartup())
        }
    }

    /**
     * 对BeanFactory完成后置处理工作
     *
     * @param beanFactory BeanFactory，支持子类去进行更多的处理
     */
    protected open fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }

    /**
     * 钩子方法，交给子类去完成，告诉子类ApplicationContext容器已经开始刷新
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
        val listerNames = getBeanNamesForType(ApplicationListener::class.java, true, false)
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
     *
     * @param beanFactory 需要去进行初始化的BeanFactory
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
     * 添加BeanFactoryPostProcessor到ApplicationContext当中
     *
     * @see BeanFactoryPostProcessor
     *
     * @param processor 你想要添加的BeanFactoryPostProcessor
     */
    override fun addBeanFactoryPostProcessor(processor: BeanFactoryPostProcessor) {
        beanFactoryPostProcessors += processor
    }

    /**
     * 添加ApplicationListener到容器当中；
     * (1)如果ApplicationEventMulticaster还没完成初始化，那么加入到ApplicationContext当中
     * (2)如果ApplicationEventMulticaster已经完成初始化，那么直接把它加入到ApplicationEventMulticaster当中
     *
     * @param listener 你想要添加的ApplicationListener监听器
     */
    override fun addApplicationListener(listener: ApplicationListener<*>) {
        if (this.applicationEventMulticaster != null) {
            getApplicationEventMulticaster().addApplicationListener(listener)
        }
        this.applicationListeners += listener
    }

    /**
     * 利用当前ApplicationContext去发布一个事件；
     * 任意对象都能支持去进行发布事件，对于不是ApplicationEvent的事件，我们将会去包装成为PayloadApplicationEvent
     *
     * @param event 要去进行发布的事件Event
     */
    override fun publishEvent(event: Any) {
        // 如果要发布的事件对象不是ApplicationEvent，需要使用PayloadApplicationEvent去进行包装一层
        val applicationEvent = if (event is ApplicationEvent) event else PayloadApplicationEvent(this, event)

        // 如果早期事件不为空，那么加入到早期事件列表当中(此时事件多拨器还没准备好，就需要一个容器去保存早期的事件)
        // 等到ApplicationEventMulticaster已经准备好了，那么就可以使用ApplicationEventMulticaster去完成事件的发布了
        if (earlyApplicationEvents != null) {
            earlyApplicationEvents!! += applicationEvent
        } else {
            getApplicationEventMulticaster().multicastEvent(applicationEvent)
        }
    }

    /**
     * 获取当前ApplicationContext的事件多拨器，提供对于事件的发布功能
     *
     * @return ApplicationEventMulticaster
     */
    open fun getApplicationEventMulticaster(): ApplicationEventMulticaster =
        this.applicationEventMulticaster
            ?: throw IllegalStateException("ApplicationContext还没完成初始化，无法获取到ApplicationEventMulticaster")

    /**
     * 获取当前[ApplicationContext]当中的所有的[ApplicationListener]
     *
     * @return Collection of ApplicationListener
     */
    open fun getApplicationListeners(): Collection<ApplicationListener<*>> = this.applicationListeners

    /**
     * 设置当前ApplicationContext的Environment
     *
     * @param environment Environment
     */
    override fun setEnvironment(environment: ConfigurableEnvironment) {
        this.environment = environment
    }

    /**
     * 获取Environment，如果不存在的话，我们在这里去创建一个默认的StandardEnvironment
     *
     * @return Environment
     */
    override fun getEnvironment(): ConfigurableEnvironment {
        if (this.environment == null) {
            this.environment = createEnvironment()
        }
        return this.environment!!
    }

    /**
     * 当前的ApplicationContext是否还活跃？
     *
     * @return 如果还没关闭，return true；否则return false
     */
    override fun isActive() = this.active.get()

    /**
     * 关闭当前的[ApplicationContext]，提供对相关的各种资源的释放；
     * * (1)destroy所有SpringBeanFactory当中的所有Bean；(并回调所有的[com.wanna.framework.beans.factory.support.DisposableBean]完成收尾工作)
     * * (2)关闭BeanFactory
     * * (3)将当前[ApplicationContext]的active标志位设置为false
     */
    override fun close() {
        // acquire startup shutdown Lock
        synchronized(this.startupShutdownMonitor) {
            doClose()  //  doClose
            if (this.shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(this.shutdownHook)
                } catch (ignored: IllegalStateException) {
                    // ignore because VM has bean closed
                }
            }
        }
    }

    /**
     * 真正地去执行close方法，关闭整个[ApplicationContext]
     */
    protected open fun doClose() {
        // 使用CAS的方式，去保证并发多线程下，只有一个线程可以去关闭当前的这个ApplicationContext
        if (this.active.get() && this.closed.compareAndSet(false, true)) {
            if (logger.isDebugEnabled) {
                logger.debug("正在关闭ApplicationContext $this")
            }

            // 1.发布ContextClosedEvent，告诉所有处理这个事件的监听器，ApplicationContext已经关闭了
            try {
                publishEvent(ContextClosedEvent(this))
            } catch (ex: Exception) {
                logger.warn("在发布ContextClosedEvent时，发生了异常", ex)
            }

            // 2.交给所有的LifecycleProcessor，去回调所有的Lifecycle Bean
            try {
                this.lifecycleProcessor?.onClose()
            } catch (ex: Exception) {
                logger.warn("在处理LifecycleProcessor时，发生了异常", ex)
            }

            // 3.摧毁掉SingletonBeanRegistry当中的所有的单实例Bean
            destroyBeans()

            // 4.关闭当前ApplicationContext当中的BeanFactory
            closeBeanFactory()

            // 5.onClose，扩展的钩子方法，如果子类需要的话，自行去重写
            onClose()

            this.active.set(false)  // set active to false
        }
    }

    /**
     * 摧毁BeanFactory当中的已经注册的所有的单实例Bean
     *
     * @see ConfigurableListableBeanFactory.destroySingletons
     */
    protected open fun destroyBeans() {
        getBeanFactory().destroySingletons()
    }

    /**
     * 关闭beanFactory，更多额外的资源回收操作，模板方法，就交给具体的子类去进行实现
     */
    protected abstract fun closeBeanFactory()

    /**
     * Spring的ApplicationContext关闭时，应该做的收尾工作；
     * 如果子类需要的话，可以借助给定的模板方法去进行更多的自定义操作
     */
    protected open fun onClose() {

    }

    /**
     * 创建Environment，默认情况下直接去创建一个StandardEnvironment，子类当中可以根据需要去进行扩展
     *
     * @return 创建出来的默认的StandardEnvironment
     */
    protected open fun createEnvironment(): ConfigurableEnvironment = StandardEnvironment()

    /**
     * 根据beanName从BeanFactory当中去获取对应的Bean
     *
     * @param beanName beanName
     * @return 从BeanFactory当中获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样的Bean的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBean(beanName: String): Any = getBeanFactory().getBean(beanName)

    /**
     * 根据beanName从BeanFactory当中去获取对应的Bean
     *
     * @param beanName beanName
     * @param args 明确给定的更多参数列表(只有在新创建一个Bean时才有效)
     * @return 从BeanFactory当中获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样的Bean的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBean(beanName: String, vararg args: Any?) = getBeanFactory().getBean(beanName, arrayOf(*args))

    /**
     * 根据beanName和beanType从BeanFactory当中去获取对应的Bean
     *
     * @param beanName beanName
     * @param type beanType
     * @param T beanType
     * @return 从BeanFactory当中获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样的Bean的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun <T> getBean(beanName: String, type: Class<T>): T = getBeanFactory().getBean(beanName, type)

    /**
     * 根据beanType从BeanFactory当中去获取到对应的Bean
     *
     * @param type beanType
     * @return 从BeanFactory当中获取到的Bean的列表
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在该类型的Bean
     * @throws NoUniqueBeanDefinitionException 如果BeanFactory当中不止一个该类型的Bean
     */
    @Throws(NoSuchBeanDefinitionException::class, NoUniqueBeanDefinitionException::class)
    override fun <T> getBean(type: Class<T>): T = getBeanFactory().getBean(type)

    /**
     * 判断给定的beanName的Bean在BeanFactory当中是否是单例的？
     *
     * @param beanName beanName
     * @return 如果是单例的return true；否则return false
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun isSingleton(beanName: String) = getBeanFactory().isSingleton(beanName)

    /**
     * 判断给定的beanName的Bean在BeanFactory当中是否是原型的？
     *
     * @param beanName beanName
     * @return 如果是原型的return true；否则return false
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun isPrototype(beanName: String) = getBeanFactory().isPrototype(beanName)

    /**
     * 根据beanName去检查BeanFactory当中该beanName对应的Bean的类型是否和给定的type匹配？
     *
     * @param name beanName
     * @param type 要去进行匹配的beanType
     */
    override fun isTypeMatch(name: String, type: Class<*>) = getBeanFactory().isTypeMatch(name, type)

    /**
     * 根据beanName获取BeanFactory当中该beanName对应的Bean的类型
     *
     * @param beanName beanName
     */
    override fun getType(beanName: String) = getBeanFactory().getType(beanName)


    /**
     * 获取当前ApplicationContext当中的BeanFactoryPostProcessor列表
     *
     * @return BeanFactoryPostProcessor列表
     */
    open fun getBeanFactoryPostProcessors(): List<BeanFactoryPostProcessor> = this.beanFactoryPostProcessors

    /**
     * 获取当前ApplicationContext的LifecycleProcessor
     *
     * @return LifecycleProcessor of this ApplicationContext
     * @throws IllegalStateException 如果LifecycleProcessor还没完成初始化的话
     */
    @Throws(IllegalStateException::class)
    open fun getLifecycleProcessor(): LifecycleProcessor =
        this.lifecycleProcessor ?: throw IllegalStateException("不存在LifecycleProcessor，请先完成初始化")

    /**
     * 获取parent ApplicationContext
     *
     * @return parent ApplicationContext(如果不存在的话return null)
     */
    override fun getParent(): ApplicationContext? = this.parent

    /**
     * 获取当前ApplicationContext的ApplicationStartup
     *
     * @return ApplicationStartup of this ApplicationContext
     */
    override fun getApplicationStartup() = this.applicationStartup

    /**
     * 根据给定的类型，从BeanFactory当中去获取所有类型匹配的Bean的列表
     *
     * @param type beanType
     * @return BeanFactory当中所有的类型匹配的Bean的列表
     */
    override fun <T> getBeansForType(type: Class<T>) = getBeanFactory().getBeansForType(type)

    /**
     * 根据给定的类型，从BeanFactory当中去获取所有类型匹配的BeanName的列表
     *
     * @param type beanType
     * @return BeanFactory当中所有的类型匹配的BeanName的列表
     */
    override fun getBeanNamesForType(type: Class<*>) = getBeanFactory().getBeanNamesForType(type)

    /**
     * 从当前BeanFactory当中去获取所有类型匹配的BeanName列表
     *
     * @param type 需要寻找的beanType
     * @param includeNonSingletons 是否需要寻找非单例的Bean？
     * @param allowEagerInit 是否渴望对一些需要懒加载的Bean去进行初始化？(比如允许FactoryBean提前getObject)
     */
    override fun getBeanNamesForType(
        type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean
    ): List<String> = getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit)

    /**
     * 使用包含当前BeanFactory以及所有的parentBeanFactory的方式去获取所有类型匹配的BeanName的列表
     *
     * @param type beanType
     * @return 从当前BeanFactory以及它的所有的parentBeanFactory当中去获取到指定类型的所有Bean
     */
    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>) =
        getBeanFactory().getBeanNamesForTypeIncludingAncestors(type)

    /**
     * 使用包含当前BeanFactory以及所有的parentBeanFactory的方式去获取所有类型匹配的BeanName列表
     *
     * @param type 需要寻找的beanType
     * @param includeNonSingletons 是否需要寻找非单例的Bean？
     * @param allowEagerInit 是否渴望对一些需要懒加载的Bean去进行初始化？(比如允许FactoryBean提前getObject)
     */
    override fun getBeanNamesForTypeIncludingAncestors(
        type: Class<*>,
        includeNonSingletons: Boolean,
        allowEagerInit: Boolean
    ): List<String> = getBeanFactory().getBeanNamesForTypeIncludingAncestors(type, includeNonSingletons, allowEagerInit)


    /**
     * 使用包含当前BeanFactory以及所有的parentBeanFactory的方式去获取所有类型匹配的Bean的列表
     *
     * @param type beanType
     * @return 从当前BeanFactory以及它的所有的parentBeanFactory当中去获取到指定类型的所有Bean
     */
    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>) =
        getBeanFactory().getBeansForTypeIncludingAncestors(type)

    /**
     * 设置parentApplicationContext
     *
     * @param parent parentApplicationContext
     */
    override fun setParent(parent: ApplicationContext?) {
        this.parent = parent
    }

    /**
     * 获取parent BeanFactory
     *
     * @return parent BeanFactory(如果不存在parent的话，那么return null)
     */
    override fun getParentBeanFactory(): BeanFactory? = getParent()

    /**
     * 设置ApplicationContext的ApplicationStartup
     *
     * @param applicationStartup ApplicationStartup
     */
    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 基于表达式的资源解析，我们直接沿用ResourcePattenResolver给定的去进行解析
     *
     * @param locationPattern 资源位置的表达式
     * @return 解析得到的资源列表
     * @throws IOException 如果解析资源失败
     */
    @Throws(IOException::class)
    override fun getResources(locationPattern: String): Array<Resource> =
        resourcePatternResolver.getResources(locationPattern)

    /**
     * 设置LifecycleProcessor，如果不指定的话，将会创建一个默认的LifecycleProcessor
     *
     * @param lifecycleProcessor 你想要使用的Lifecycle
     * @see DefaultLifecycleProcessor
     */
    open fun setLifecycleProcessor(@Nullable lifecycleProcessor: LifecycleProcessor?) {
        this.lifecycleProcessor = lifecycleProcessor
    }
}