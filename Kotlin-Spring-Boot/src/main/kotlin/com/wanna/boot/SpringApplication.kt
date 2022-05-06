package com.wanna.boot

import com.wanna.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.support.AbstractApplicationContext
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.util.AnnotationConfigUtils
import com.wanna.framework.core.util.ClassUtils
import org.slf4j.LoggerFactory

/**
 * 这是一个SpringApplication的启动类，交由它去进行引导整个SpringApplication的启动
 */
open class SpringApplication(vararg _primarySources: Class<*>) {
    companion object {
        /**
         * 提供静态方法，去运行SpringApplication
         *
         * @param primarySource 配置类
         * @param args 命令行参数
         */
        @JvmStatic
        fun run(primarySource: Class<*>, vararg args: String): ConfigurableApplicationContext {
            return run(arrayOf(primarySource), args.toList().toTypedArray())
        }

        /**
         * 提供静态方法，去运行SpringApplication
         *
         * @param primarySources 配置类列表
         * @param args 命令行参数
         */
        @JvmStatic
        fun run(primarySources: Array<Class<*>>, args: Array<String>): ConfigurableApplicationContext {
            return SpringApplication(*primarySources).run(*args)
        }
    }

    // primarySources
    private var primarySources: Set<Class<*>>? = LinkedHashSet(_primarySources.toList())

    // Application的监听器列表
    private var listeners: MutableList<ApplicationListener<*>> =
        SpringFactoriesLoader.loadFactories(ApplicationListener::class.java)

    // SpringApplicationType
    private var applicationType = ApplicationType.deduceFromClassPath()

    // beanNameGenerator
    private var beanNameGenerator: BeanNameGenerator? = null

    // environment
    private var environment: ConfigurableEnvironment? = null

    // 这种一个BootstrapWrapper注册中心，完成对BootstrapContext去进行初始化
    private var bootstrappers: MutableList<Bootstrapper> = SpringFactoriesLoader.loadFactories(Bootstrapper::class.java)

    // SpringApplication的ApplicationContext的初始化器，在ApplicationContext完成创建和初始化工作时，会自动完成回调
    private var initializers: MutableList<ApplicationContextInitializer<*>> =
        SpringFactoriesLoader.loadFactories(ApplicationContextInitializer::class.java)

    // 推测SpringApplication的主启动类
    private val mainApplicationClass: Class<*> = deduceMainApplicationClass()

    // 是否需要添加ConversionService到容器当中？
    private var addConversionService = true

    // ApplicationStartup，支持对SpringApplication启动过程中的各个阶段去进行记录，支持去进行自定义，从而完成自定义的功能
    private var applicationStartup = ApplicationStartup.DEFAULT

    // 打印Banner的模式，NO/CONSOLE/LOG
    private var bannerMode: Banner.Mode = Banner.Mode.CONSOLE

    // logger
    private var logger = LoggerFactory.getLogger(SpringApplication::class.java)

    // banner
    private var banner: Banner? = null

    /**
     * 获取SpringApplication当中的监听器列表，并完成好排序工作
     *
     * @return 排好序的ApplicationListener列表
     */
    open fun getListeners(): Set<ApplicationListener<*>> {
        return asOrderSet(this.listeners)
    }

    /**
     * 引导整个SpringApplication的启动
     *
     * @param args 命令行参数列表
     * @return 完成刷新工作的ApplicationContext
     */
    open fun run(vararg args: String): ConfigurableApplicationContext {
        // 创建BootstrapContext
        val bootstrapContext = createBootstrapContext()

        // SpringApplication的ApplicationContext
        var applicationContext: ConfigurableApplicationContext? = null
        // 获取SpringApplicationRunListeners
        val listeners: SpringApplicationRunListeners = getRunListeners(arrayOf(*args))
        // 通知所有的监听器，当前SpringApplication已经正在启动当中了...
        listeners.starting(bootstrapContext)

        try {
            val applicationArguments = DefaultApplicationArguments(arrayOf(*args))

            // 准备好SpringApplication环境
            val environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments)

            // 环境已经准备好了，可以去打印Banner了，并将创建的Banner去进行返回1
            val banner = printBanner(environment)
            // 创建ApplicationContext并且设置ApplicationStartup对象到ApplicationContext当中
            applicationContext = createApplicationContext()
            applicationContext.setApplicationStartup(this.applicationStartup)

            // 准备SpringApplication的ApplicationContext
            prepareContext(bootstrapContext, applicationContext, environment, listeners, applicationArguments, banner)

            // 刷新SpringApplication的ApplicationContext
            refreshContext(applicationContext)

            // 在SpringApplication的ApplicationContext完成刷新之后的回调，是一个钩子函数，交给子类去完成
            afterRefresh(applicationContext, applicationArguments)

            // 通知所有的监听器，SpringApplication的已经启动完成，可以去进行后置处理工作了
            listeners.started(applicationContext)

            // 拿出容器当中的所有的ApplicationRunner和CommandLineRunner，去进行回调
            callRunners(applicationContext, applicationArguments)
        } catch (ex: Throwable) {
            handleRunException(applicationContext, ex, listeners)
            throw IllegalStateException(ex)
        }
        try {
            // 通知所有的监听器，SpringApplication已经正在运行当中了，可以去进行后置处理工作了
            listeners.running(applicationContext)
        } catch (ex: Throwable) {
            handleRunException(applicationContext, ex, null)
            throw IllegalStateException(ex)
        }
        return applicationContext
    }

    /**
     * 准备SpringApplication的ApplicationContext，将Environment设置到ApplicationContext当中，并完成ApplicationContext的初始化工作；
     * 将注册到SpringApplication当中的配置类注册到ApplicationContext当中
     */
    protected open fun prepareContext(
        bootstrapContext: BootstrapContext,
        context: ConfigurableApplicationContext,
        environment: ConfigurableEnvironment,
        listeners: SpringApplicationRunListeners,
        arguments: ApplicationArguments,
        banner: Banner?
    ) {
        // 将准备好的环境对象，设置到ApplicationContext当中去
        context.setEnvironment(environment)

        // 完成ApplicationContext的后置处理工作，给容器中注册beanNameGenerator和conversionService
        postProcessApplicationContext(context)

        // 调用所有的ApplicationContextInitializer去完成ApplicationContext的初始化
        applyInitializers(context)

        // 通知监听器，ApplicationContext已经准备好了，可以完成后置处理了
        listeners.contextPrepared(context)

        // 从容器当中获取到BeanFactory
        val beanFactory = context.getBeanFactory()

        // 把ApplicationArguments注册到容器当中
        beanFactory.registerSingleton("applicationArguments", arguments)

        // 如果设置了Banner的话，将Banner注册到beanFactory当中
        if (banner != null) {
            // 将Banner对象注册到容器当中
            beanFactory.registerSingleton("springBootBanner", banner)
        }

        // 把注册到SpringApplication当中的source去完成BeanDefinition的加载
        load(context, primarySources!!.toTypedArray())

        // 通知监听器ApplicationContext已经启动完成了，可以完成后置处理工作了
        listeners.contextLoaded(context)
    }

    /**
     * 根据设置的BannerNode的不同，使用不同的方式去完成SpringApplication的Banner的打印
     *
     * @param environment Environment
     * @return 如果BannerMode=NO，return null；否则return 创建好的Banner
     */
    protected open fun printBanner(environment: ConfigurableEnvironment): Banner? {
        if (bannerMode == Banner.Mode.NO) {
            return null
        }
        val springBootBannerPrinter = SpringApplicationBannerPrinter(banner)
        if (bannerMode == Banner.Mode.CONSOLE) {
            return springBootBannerPrinter.print(environment, this.mainApplicationClass, System.out)
        }
        return springBootBannerPrinter.print(environment, mainApplicationClass, logger)
    }

    /**
     * 处理启动SpringApplication过程当中的异常
     */
    protected open fun handleRunException(
        applicationContext: ConfigurableApplicationContext?, ex: Throwable, listeners: SpringApplicationRunListeners?
    ) {
        listeners?.failed(applicationContext, ex)
    }

    /**
     * 创建BootstrapContext，并回调所有的Bootstrapper去完成初始化
     *
     * @return 创建好的BootstrapContext
     */
    protected open fun createBootstrapContext(): ConfigurableBootstrapContext {
        val bootstrapContext = DefaultBootstrapContext()
        this.bootstrappers.forEach { it.initialize(bootstrapContext) }
        return bootstrapContext
    }

    /**
     * 对ApplicationContext去进行后置处理工作，可以注册BeanNameGenerator、添加ConversionService等
     *
     * @param context ApplicationContext
     */
    protected open fun postProcessApplicationContext(context: ConfigurableApplicationContext) {
        // 如果设置了beanNameGenerator的话，那么需要将它注册到容器当中
        if (this.beanNameGenerator != null) {
            context.getBeanFactory()
                .registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, this.beanNameGenerator!!)
        }

        // 如果要添加ConversionService的话，设置到BeanFactory当中
        if (this.addConversionService) {
            context.getBeanFactory().setConversionService(DefaultConversionService.getSharedInstance())
        }
    }

    /**
     * 回调所有的Runner，拿出容器当中所有的ApplicationRunner和CommandLineRunner，去进行排序，并进行执行
     *
     * @see ApplicationRunner
     * @see CommandLineRunner
     */
    protected open fun callRunners(applicationContext: ApplicationContext, applicationArguments: ApplicationArguments) {
        val runners = ArrayList<Any>()
        runners += applicationContext.getBeansForType(ApplicationRunner::class.java)
        runners += applicationContext.getBeansForType(CommandLineRunner::class.java)
        AnnotationAwareOrderComparator.sort(runners)
        runners.forEach {
            if (it is ApplicationRunner) {
                it.run(applicationArguments)
            }
            if (it is CommandLineRunner) {
                it.run(applicationArguments.getSourceArgs())
            }
        }
    }

    /**
     * 获取SpringApplication的ApplicationContext的初始化器列表
     */
    protected open fun getInitializers(): Set<ApplicationContextInitializer<*>> {
        return asOrderSet(initializers)
    }

    /**
     * 遍历所有的ApplicationContext的Initializer，去完成ApplicationContext的应用
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun applyInitializers(context: ConfigurableApplicationContext) {
        getInitializers().forEach {
            val initializer = it as ApplicationContextInitializer<ConfigurableApplicationContext>
            initializer.initialize(context)
        }
    }

    /**
     * 将一个列表当中的元素转换为有序的Set(LinkedHashSet)
     *
     * @param elements 要进行排序的集合
     * @return 排好序的集合，并转换为Set去进行return
     */
    private fun <T> asOrderSet(elements: Collection<T>): Set<T> {
        val list = ArrayList(elements)
        AnnotationAwareOrderComparator.sort(list)
        return LinkedHashSet(list)
    }

    /**
     * 将source当中的配置类等信息加载到ApplicationContext当中
     *
     * @param context ApplicationContext
     * @param sources 注册到SpringApplication当中的source信息
     */
    private fun load(context: ApplicationContext, sources: Array<*>) {
        // 从ApplicationContext当中去获取BeanDefinitionRegistry，并创建BeanDefinitionLoader去完成BeanDefinition的加载
        val registry = getBeanDefinitionRegistry(context)
        val beanDefinitionLoader = BeanDefinitionLoader(registry, sources)
        if (this.beanNameGenerator != null) {
            beanDefinitionLoader.setBeanNameGenerator(this.beanNameGenerator!!)
        }
        if (this.environment != null) {
            beanDefinitionLoader.setEnvironment(this.environment!!)
        }
        // 交给BeanDefinitionLoader去完成BeanDefinition的加载
        beanDefinitionLoader.load()
    }

    /**
     * 从ApplicationContext当中去获取到BeanDefinitionRegistry
     */
    private fun getBeanDefinitionRegistry(context: ApplicationContext): BeanDefinitionRegistry {
        if (context is BeanDefinitionRegistry) {
            return context
        }
        if (context is AbstractApplicationContext) {
            return context.getBeanFactory() as BeanDefinitionRegistry
        }
        throw IllegalStateException("无法从这样的类型[type=${context::class.java}]的ApplicationContext当中去获取到BeanDefinitionRegistry")
    }

    /**
     * 在完成ApplicationContext之后的回调函数，交给子类去完成
     */
    protected open fun afterRefresh(
        context: ConfigurableApplicationContext, applicationArguments: ApplicationArguments
    ) {

    }

    /**
     * 刷新SpringApplication的ApplicationContext
     */
    protected open fun refreshContext(context: ConfigurableApplicationContext) {
        context.refresh()
    }

    /**
     * 根据ApplicationType，去创建对应类型的Spring应用的ApplicationContext
     */
    protected open fun createApplicationContext(): ConfigurableApplicationContext {
        return when (this.applicationType) {
            ApplicationType.NONE -> AnnotationConfigApplicationContext()
            ApplicationType.SERVLET -> AnnotationConfigApplicationContext()
            ApplicationType.REACTIVE -> AnnotationConfigReactiveWebServerApplicationContext()
        }
    }

    /**
     * 准备环境
     */
    protected open fun prepareEnvironment(
        listeners: SpringApplicationRunListeners,
        bootstrapContext: ConfigurableBootstrapContext,
        applicationArguments: ApplicationArguments
    ): ConfigurableEnvironment {
        val environment = getOrCreateEnvironment()

        // 通知所有的监听器，环境已经准备好了，可以去完成后置处理了...
        listeners.environmentPrepared(bootstrapContext, environment)

        return environment
    }

    /**
     * 如果SpringApplication配置了Environment，那么使用自定义的Environment；
     * 如果没有配置Environment，那么就根据applicationType去新创建一个Environment对象
     */
    private fun getOrCreateEnvironment(): ConfigurableEnvironment {
        if (this.environment != null) {
            return this.environment!!
        }
        return when (applicationType) {
            ApplicationType.NONE -> StandardEnvironment()
            ApplicationType.SERVLET -> StandardEnvironment()
            ApplicationType.REACTIVE -> StandardEnvironment()
        }
    }

    /**
     * 获取SpringApplicationRunListeners
     */
    protected open fun getRunListeners(args: Array<String>): SpringApplicationRunListeners {
        val factoryNames = SpringFactoriesLoader.loadFactoryNames(SpringApplicationRunListener::class.java)
        val runListeners = SpringFactoriesLoader.createSpringFactoryInstances(
            SpringApplicationRunListener::class.java,
            arrayOf(SpringApplication::class.java, Array<String>::class.java),
            null,
            arrayOf(this, args),
            LinkedHashSet(factoryNames)
        )
        return SpringApplicationRunListeners(runListeners, args)
    }

    /**
     * 探测SpringApplication的启动类
     */
    private fun deduceMainApplicationClass(): Class<*> {
        java.lang.RuntimeException().stackTrace.forEach {
            if (it.methodName == "main") {
                return ClassUtils.forName<Any>(it.className)
            }
        }
        return null!!
    }

    /**
     * 设置SpringApplication的Initializers，完成SpringApplication的ApplicationContext的初始化
     */
    open fun setInitializers(initializers: Collection<ApplicationContextInitializer<*>>) {
        this.initializers = ArrayList(initializers)
    }

    /**
     * 设置SpringApplication的ApplicationListener
     */
    open fun setApplicationListeners(listeners: Collection<ApplicationListener<*>>) {
        this.listeners = ArrayList(listeners)
    }

    /**
     * 设置SpringApplication中对于BootstrapContext的初始化工作的BootstrapWrapper
     */
    open fun setBootstrappers(bootstrappers: Collection<Bootstrapper>) {
        this.bootstrappers = ArrayList(bootstrappers)
    }

    /**
     * 添加ApplicationContext的Initializer
     */
    open fun addInitializer(initializer: ApplicationContextInitializer<*>) {
        this.initializers += initializer
    }

    /**
     * 添加ApplicationContextInitializer列表
     */
    open fun addInitializers(initializers: Collection<ApplicationContextInitializer<*>>) {
        this.initializers += initializers
    }

    /**
     * 添加ApplicationListener列表到SpringApplication当中
     */
    open fun addApplicationListeners(listeners: Collection<ApplicationListener<*>>) {
        this.listeners += listeners
    }

    /**
     * 添加Bootstrapper列表到SpringApplication当中
     */
    open fun addBootstrappers(bootstrappers: Collection<Bootstrapper>) {
        this.bootstrappers += bootstrappers
    }

    /**
     * 提供SpringApplication的ApplicationStartup的设置，在SpringApplication启动过程当中，会自动将
     * ApplicationStartup对象设置到SpringApplication的ApplicationContext当中，可以替换自定义的ApplicationStartup，
     * 去实现更多相关的自定义功能，比如可以替换一个进行日志的输出工作的ApplicationStartup
     */
    open fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 设置Banner打印模式，Console/Log/No
     */
    open fun setBannerMode(mode: Banner.Mode) {
        this.bannerMode = mode
    }

    /**
     * 自定义去进行设置ApplicationType
     */
    open fun setApplicationType(applicationType: ApplicationType) {
        this.applicationType = applicationType
    }
}