package com.wanna.boot

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.support.AbstractApplicationContext
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.environment.*
import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.util.AnnotationConfigUtils
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StopWatch
import com.wanna.framework.util.StringUtils.collectionToCommaDelimitedString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 这是一个SpringApplication的启动类，交由它去进行引导整个SpringApplication的启动
 *
 * @param resourceLoader 资源加载器，提供资源的加载
 * @param _primarySources 启动类列表
 */
open class SpringApplication(private var resourceLoader: ResourceLoader?, vararg _primarySources: Class<*>) {
    companion object {

        /**
         * SpringBoot的Banner的属性值，可以在配置文件/命令行参数当中去通过这个属性值去配置Banner的位置
         */
        const val BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY

        /**
         * 默认的SpringBootBanner的位置
         */
        const val DEFAULT_BANNER_LOCATION = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION

        /**
         * 用于创建MVC的ApplicationContext的Class
         */
        const val DEFAULT_MVC_WEB_CONTEXT_CLASS =
            "com.wanna.boot.web.mvc.context.AnnotationConfigMvcWebServerApplicationContext"

        /**
         * 用于创建Servlet的ApplicationContext的Class
         */
        const val DEFAULT_SERVLET_WEB_CONTEXT_CLASS =
            "com.wanna.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext"

        /**
         * 用于创建默认的ApplicationContext的Class
         */
        const val DEFAULT_CONTEXT_CLASS =
            "com.wanna.framework.context.annotation.AnnotationConfigApplicationContext"

        /**
         * 当中Runtime(JVM)关闭(Shutdown)时的回调钩子方法
         *
         * @see SpringApplicationShutdownHook
         */
        @JvmStatic
        val shutdownHook = SpringApplicationShutdownHook()

        /**
         * 提供静态方法，去运行SpringApplication
         *
         * @param primarySource 配置类
         * @param args 命令行参数
         * @return SpringApplication构建好的ApplicationContext
         */
        @JvmStatic
        fun run(primarySource: Class<*>, vararg args: String): ConfigurableApplicationContext {
            return run(arrayOf(primarySource), arrayOf(*args))
        }

        /**
         * 提供静态方法，去运行SpringApplication
         *
         * @param primarySources 配置类列表
         * @param args 命令行参数
         * @return SpringApplication构建好的ApplicationContext
         */
        @JvmStatic
        fun run(primarySources: Array<Class<*>>, args: Array<String>): ConfigurableApplicationContext {
            return SpringApplication(*primarySources).run(*args)
        }

        /**
         * 对外提供一个main方法，支持去直接启动SpringApplication；
         * 直接不指定主启动类，适用于那些将启动类写到命令行参数("--spring.main.sources")当中的情况；
         *
         * 很多开发者可能会选择自定义一个main方法，然后自己去调用run方法去启动SpringApplication
         *
         * @param args 命令行参数
         * @throws Exception 如果启动Spring应用失败的话
         * @see run
         */
        @JvmStatic
        @Throws(Exception::class)
        fun main(vararg args: String) {
            run(emptyArray(), arrayOf(*args))
        }

        /**
         * 对外提供的一个static方法，用于去退出一个Spring应用，并生成ExitCode；
         * 退出一个Spring应用时，关闭ApplicationContext，发布ExitCodeEvent事件，并获取到ExitCode
         *
         * @param context 要关闭的ApplicationContext
         * @param exitCodeGenerators ExitCodeGenerator列表
         * @return ExitCode
         */
        @JvmStatic
        fun exit(context: ApplicationContext, vararg exitCodeGenerators: ExitCodeGenerator): Int {
            var exitCode = 0
            try {
                try {
                    val generators = ExitCodeGenerators()
                    val beans = context.getBeansForType(ExitCodeGenerator::class.java).values
                    generators.addAll(beans)
                    generators.addAll(*exitCodeGenerators)
                    exitCode = generators.getExitCode()
                    if (exitCode != 0) {
                        context.publishEvent(ExitCodeEvent(context, exitCode))
                    }
                } finally {
                    close(context)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                exitCode = if (exitCode != 0) exitCode else 1
            }
            return exitCode
        }

        /**
         * 关闭一个ApplicationContext
         *
         * @param context 要去进行关闭的ApplicationContext
         */
        @JvmStatic
        private fun close(context: ApplicationContext) {
            if (context is ConfigurableApplicationContext) {
                context.close()
            }
        }
    }

    /**
     * 提供一个不用ResourceLoader的构造器，只去指定primarySources
     *
     * @param _primarySources 主启动类
     */
    constructor(vararg _primarySources: Class<*>) : this(null as ResourceLoader?, *_primarySources)

    /**
     * 提供一个无参数构造器
     */
    constructor() : this(_primarySources = emptyArray())

    /**
     * primarySources
     */
    private var primarySources: MutableSet<Class<*>> = LinkedHashSet(_primarySources.toList())

    /**
     * sources
     */
    private var sources: MutableCollection<Any> = LinkedHashSet()

    /**
     * Application的监听器列表
     */
    private var listeners: MutableList<ApplicationListener<*>> =
        SpringFactoriesLoader.loadFactories(ApplicationListener::class.java)

    /**
     * Spring的ApplicationType，自动从类路径当中的依赖去进行推断
     */
    private var applicationType = ApplicationType.deduceFromClassPath()

    /**
     * beanNameGenerator
     */
    private var beanNameGenerator: BeanNameGenerator? = null

    /**
     * 用来创建ApplicationContext的类，如果不指定的话，将会根据ApplicationType去进行自动推断
     *
     * @see applicationType
     */
    private var applicationContextClass: Class<out ConfigurableApplicationContext>? = null

    /**
     * Environment
     */
    private var environment: ConfigurableEnvironment? = null

    /**
     * 这种一个Bootstrapper注册中心，用来完成对BootstrapContext去进行初始化
     */
    private var bootstrappers: MutableCollection<Bootstrapper> = getSpringFactoriesInstances(Bootstrapper::class.java)

    /**
     * SpringApplication的ApplicationContext的初始化器，在ApplicationContext完成创建和初始化工作时，会自动完成回调
     */
    private var initializers: MutableCollection<ApplicationContextInitializer<*>> =
        getSpringFactoriesInstances(ApplicationContextInitializer::class.java)

    /**
     * 推测SpringApplication的主启动类
     */
    private var mainApplicationClass: Class<*>? = deduceMainApplicationClass()

    /**
     * 是否允许BeanDefinition去发生覆盖？
     */
    private var allowBeanDefinitionOverriding: Boolean = true

    /**
     * 是否需要添加ConversionService到容器当中？
     */
    private var addConversionService = true

    /**
     * ApplicationStartup，支持对SpringApplication启动过程中的各个阶段去进行记录，支持去进行自定义，从而完成自定义的功能
     */
    private var applicationStartup = ApplicationStartup.DEFAULT

    /**
     * 打印Banner的模式，NO/CONSOLE/LOG
     */
    private var bannerMode: Banner.Mode = Banner.Mode.CONSOLE

    /**
     * logger
     */
    private var logger = LoggerFactory.getLogger(SpringApplication::class.java)

    /**
     * banner
     */
    private var banner: Banner? = null

    /**
     * 是否需要打印SpringApplication启动的相关信息? 比如profile信息
     */
    private var logStartupInfo = true

    /**
     * 是否需要去注册ShutdownHook？默认为true
     */
    private var registerShutdownHook = true

    /**
     * 获取SpringApplication当中的监听器列表，并完成好排序工作
     *
     * @return 排好序的ApplicationListener列表
     */
    open fun getListeners(): Set<ApplicationListener<*>> = asOrderSet(this.listeners)

    /**
     * 引导整个SpringApplication的启动
     *
     * @param args 命令行参数列表
     * @return 完成刷新工作的ApplicationContext
     */
    open fun run(vararg args: String): ConfigurableApplicationContext {
        // 开启秒表的计时，方便去统计整个应用启动过程当中的占用的时间
        val stopWatch = StopWatch()
        stopWatch.start()

        // 创建BootstrapContext
        val bootstrapContext = createBootstrapContext()

        // SpringApplication的ApplicationContext
        var applicationContext: ConfigurableApplicationContext? = null

        // SpringBoot的ExceptionReporter列表
        var exceptionReporters: Collection<SpringBootExceptionReporter> = ArrayList()

        // 获取SpringApplicationRunListeners
        val listeners: SpringApplicationRunListeners = getRunListeners(arrayOf(*args))
        // 通知所有的监听器，当前SpringApplication已经正在启动当中了...
        listeners.starting(bootstrapContext, this.mainApplicationClass)

        try {
            val applicationArguments = DefaultApplicationArguments(arrayOf(*args))

            // 准备好SpringApplication环境
            val environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments)

            // 环境已经准备好了，可以去打印SpringBoot的Banner了，并将创建的Banner去进行返回
            val banner = printBanner(environment)
            // 创建ApplicationContext并且设置ApplicationStartup对象到ApplicationContext当中
            applicationContext = createApplicationContext()
            applicationContext.setApplicationStartup(this.applicationStartup)

            exceptionReporters = getSpringFactoriesInstances(
                SpringBootExceptionReporter::class.java,
                arrayOf(ConfigurableApplicationContext::class.java),
                applicationContext
            )
            // 准备SpringApplication的ApplicationContext
            prepareContext(bootstrapContext, applicationContext, environment, listeners, applicationArguments, banner)

            // 刷新SpringApplication的ApplicationContext
            refreshContext(applicationContext)

            // 在SpringApplication的ApplicationContext完成刷新之后的回调，是一个钩子函数，交给子类去完成
            afterRefresh(applicationContext, applicationArguments)

            // 结束秒表的计时
            stopWatch.stop()

            // 如果需要记录startup的相关信息的话
            if (this.logStartupInfo) {
                StartupInfoLogger(mainApplicationClass).logStarted(getApplicationLogger(), stopWatch)
            }

            // 通知所有的监听器，SpringApplication的已经启动完成，可以去进行后置处理工作了
            listeners.started(applicationContext)

            // 拿出容器当中的所有的ApplicationRunner和CommandLineRunner，去进行回调，处理命令行参数
            callRunners(applicationContext, applicationArguments)
        } catch (ex: Throwable) {
            handleRunFailure(applicationContext, ex, listeners, exceptionReporters)
            throw IllegalStateException(ex)
        }
        try {
            // 通知所有的监听器，SpringApplication已经正在运行当中了，可以去进行后置处理工作了
            listeners.running(applicationContext)
        } catch (ex: Throwable) {
            handleRunFailure(applicationContext, ex, null, exceptionReporters)
            throw IllegalStateException(ex)
        }
        return applicationContext
    }

    /**
     * 准备SpringApplication的ApplicationContext，将Environment设置到ApplicationContext当中，并完成ApplicationContext的初始化工作；
     * 将注册到SpringApplication当中的配置类注册到ApplicationContext当中
     *
     * @param bootstrapContext bootstrapContext
     * @param context ApplicationContext
     * @param environment Environment
     * @param listeners listeners
     * @param arguments ApplicationArguments
     * @param banner banner(有可能为空)
     */
    protected open fun prepareContext(
        bootstrapContext: DefaultBootstrapContext,
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

        // 当ApplicationContext已经准备好了，可以去关闭BootstrapContext了
        // 回调BootstrapContext当中所有处理BootstrapContextClosed事件的监听器
        bootstrapContext.close(context)

        // SpringApplication的整个ApplicationContext已经准备好了，可以去进行打印相关的日志信息了
        // 是否需要打印SpringApplication启动过程当中的相关信息，如果需要的话，需要在这里去打印SpringApplication的相关环境信息
        if (this.logStartupInfo) {
            // 只要root容器才打印启动的相关信息
            logStartingInfo(context.getParent() == null)

            // 输出启动过程当中的Profile信息..
            logStartupProfileInfo(context)
        }

        // 从SpringApplication的ApplicationContext当中获取到BeanFactory
        val beanFactory = context.getBeanFactory()
        if (beanFactory is DefaultListableBeanFactory) {
            beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding)
        }
        // 把ApplicationArguments注册到beanFactory当中
        beanFactory.registerSingleton("applicationArguments", arguments)

        // 如果使用到了Banner的话，将Banner也注册到beanFactory当中
        if (banner != null) {
            // 将Banner对象注册到容器当中
            beanFactory.registerSingleton("springBootBanner", banner)
        }

        // 把注册到SpringApplication当中的source去完成BeanDefinition的加载
        load(context, getAllSources().toTypedArray())

        // 通知监听器ApplicationContext已经启动完成了，可以完成后置处理工作了
        listeners.contextLoaded(context)
    }

    /**
     * 根据设置的BannerNode的不同，使用不同的方式去完成SpringApplication的Banner的打印
     *
     * * 1.如果Mode=No, 那么不输出Banner
     * * 2.如果Mode=CONSOLE, 将会才控制台输出Banner
     * * 3.如果Mode=LOG, 将会使用Logger的方式去输出Banner
     *
     * @param environment Environment
     * @return 如果BannerMode=NO，return null；否则return 创建好的Banner
     */
    protected open fun printBanner(environment: ConfigurableEnvironment): Banner? {
        if (bannerMode == Banner.Mode.NO) {
            return null
        }
        // 如果指定了ResourceLoader，那么使用给定的；否则使用默认的
        val resourceLoader = this.resourceLoader ?: DefaultResourceLoader(getClassLoader())
        val springBootBannerPrinter = SpringApplicationBannerPrinter(resourceLoader, banner)
        if (bannerMode == Banner.Mode.CONSOLE) {
            return springBootBannerPrinter.print(environment, this.mainApplicationClass, System.out)
        }
        return springBootBannerPrinter.print(environment, mainApplicationClass, logger)
    }

    /**
     * 处理启动SpringApplication过程当中的异常
     *
     * @param applicationContext ApplicationContext
     * @param ex  Throwable(运行Spring应用过程当中的异常信息)
     * @param listeners SpringApplicationRunListener
     * @param reporters SpringBoot的异常报告器
     */
    protected open fun handleRunFailure(
        applicationContext: ConfigurableApplicationContext?, ex: Throwable, listeners: SpringApplicationRunListeners?,
        reporters: Collection<SpringBootExceptionReporter>
    ) {
        try {
            listeners?.failed(applicationContext, ex)
        } finally {
            reportFailure(reporters, ex)
        }
    }

    /**
     * 使用SpringBoot的ExceptionReporter去报告启动过程当中的异常
     *
     * @param exceptionReporters SpringBootExceptionReporter列表
     * @param failure 要去进行报告的异常
     */
    private fun reportFailure(exceptionReporters: Collection<SpringBootExceptionReporter>, failure: Throwable) {
        try {
            exceptionReporters.forEach {
                if (it.report(failure)) {
                    return
                }
            }
        } finally {

        }
        if (logger.isErrorEnabled) {
            logger.error("Spring应用运行失败", failure)
        }
    }

    /**
     * 创建BootstrapContext，并回调所有的Bootstrapper去完成初始化
     *
     * @return 创建好的BootstrapContext
     */
    protected open fun createBootstrapContext(): DefaultBootstrapContext {
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
        // 如果设置了ResourceLoader，那么将它设置给ApplicationContext
        if (this.resourceLoader != null) {
            if (context is GenericApplicationContext) {
                context.setResourceLoader(this.resourceLoader!!)
            }
            if (context is DefaultResourceLoader) {
                context.setClassLoader(this.resourceLoader!!.getClassLoader())
            }
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
     *
     * @param applicationArguments 命令行参数信息
     * @param applicationContext ApplicationContext
     */
    protected open fun callRunners(applicationContext: ApplicationContext, applicationArguments: ApplicationArguments) {
        val runners = ArrayList<Any>()
        // fixed:要添加的只是Value而已，而不是Map<String,T>
        runners.addAll(applicationContext.getBeansForType(ApplicationRunner::class.java).values)
        runners.addAll(applicationContext.getBeansForType(CommandLineRunner::class.java).values)
        AnnotationAwareOrderComparator.sort(runners)  // sort
        // 去重，并回调所有的Runner
        LinkedHashSet(runners).forEach {
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
    open fun getInitializers(): Set<ApplicationContextInitializer<*>> {
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
     * 根据type去获取SpringFactories当中的实例
     *
     * @param type 要从SpringFactories当中去进行获取的type
     * @return 从SpringFactories当中加载到的对象列表
     */
    private fun <T> getSpringFactoriesInstances(
        type: Class<T>,
    ): MutableCollection<T> = getSpringFactoriesInstances(type, emptyArray())

    /**
     * 根据type去获取SpringFactories当中的实例
     *
     * @param type 要从SpringFactories当中去进行获取的type
     * @param parameterTypes 构造器参数类型列表
     * @param args parameterTypes对应的构造器参数对象
     * @return 从SpringFactories当中加载到的对象列表
     */
    private fun <T> getSpringFactoriesInstances(
        type: Class<T>,
        parameterTypes: Array<Class<*>>,
        vararg args: Any
    ): MutableCollection<T> {
        val classLoader = getClassLoader()
        val names: Set<String> =
            java.util.LinkedHashSet(
                SpringFactoriesLoader.loadFactoryNames(type, classLoader)
            )
        val instances = SpringFactoriesLoader.createSpringFactoryInstances(
            type, parameterTypes, classLoader,
            arrayOf(*args), names
        )
        AnnotationAwareOrderComparator.sort(instances)
        return instances
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
     *
     * @param context 要去进行刷新的ApplicationContext
     */
    private fun refreshContext(context: ConfigurableApplicationContext) {
        // 如果需要注册ShutdownHook的话，那么把当前ApplicationContext去进行注册到ShutdownHook当中
        if (registerShutdownHook) {
            shutdownHook.registerApplicationContext(context)
        }
        refresh(context)
    }

    /**
     * 刷新Spring的ApplicationContext
     *
     * @param context 要去进行刷新的ApplicationContext
     */
    protected open fun refresh(context: ConfigurableApplicationContext) {
        context.refresh()
    }

    /**
     * 根据ApplicationType，去创建对应类型的Spring应用的ApplicationContext
     *
     * @return 创建好的ApplicationContext对象
     */
    protected open fun createApplicationContext(): ConfigurableApplicationContext {
        // 如果指定了ApplicationContextClass，那么使用给定的ApplicationContextClass去创建对象
        var applicationContextClass = this.applicationContextClass
        // 如果没有指定ApplicationContextClass，那么将会根据ApplicationType去进行推断
        if (applicationContextClass == null) {
            try {
                applicationContextClass = when (this.applicationType) {
                    ApplicationType.NONE -> ClassUtils.forName(DEFAULT_CONTEXT_CLASS)
                    ApplicationType.SERVLET -> ClassUtils.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS)
                    ApplicationType.MVC -> ClassUtils.forName(DEFAULT_MVC_WEB_CONTEXT_CLASS)
                }
            } catch (ex: ClassNotFoundException) {
                throw IllegalStateException("无法根据给定的ApplicationType去推断出来合适的ApplicationContext的类名，请先指定ApplicationContextClass")
            }
        }
        return BeanUtils.instantiateClass(applicationContextClass)
    }

    /**
     * 准备环境
     *
     * @param listeners SpringApplication的监听器的触发器
     * @param bootstrapContext Bootstrap的上下文信息
     * @param applicationArguments 应用程序的启动参数列表
     */
    protected open fun prepareEnvironment(
        listeners: SpringApplicationRunListeners,
        bootstrapContext: ConfigurableBootstrapContext,
        applicationArguments: ApplicationArguments
    ): ConfigurableEnvironment {
        val environment = getOrCreateEnvironment()

        // 配置环境，添加ConversionService以及命令行参数的PropertySource
        configureEnvironment(environment, applicationArguments.getSourceArgs())
        // 通知所有的监听器，环境已经准备好了，可以去完成后置处理了...
        listeners.environmentPrepared(bootstrapContext, environment)
        bindToSpringApplication(environment)

        return environment
    }

    /**
     * 将配置文件当中的内容，去绑定到SpringApplication对象当中来
     *
     * @param environment Environment
     */
    protected open fun bindToSpringApplication(environment: Environment) {

    }

    /**
     * 对SpringApplication Environment环境去进行配置
     *
     * @param environment 要去进行配置的环境
     * @param args 命令行参数列表
     */
    protected open fun configureEnvironment(environment: ConfigurableEnvironment, args: Array<String>) {
        if (this.addConversionService) {
            environment.setConversionService(DefaultConversionService.getSharedInstance())
        }
        configurePropertySources(environment, args)
    }

    /**
     * 对SpringApplication的Environment的PropertySource去进行配置
     *
     * @param environment 要去进行配置的环境
     * @param args 命令行参数列表
     */
    protected open fun configurePropertySources(environment: ConfigurableEnvironment, args: Array<String>) {
        val sourceName = SimpleCommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME
        if (environment.getPropertySources().contains(sourceName)) {
            val old = environment.getPropertySources().get(sourceName)!!
            val newSource = CompositePropertySource(sourceName)
            newSource.addPropertySource(old)
            newSource.addPropertySource(SimpleCommandLinePropertySource("springApplicationCommandLineArgs", *args))
            environment.getPropertySources().replace(sourceName, newSource)  // replace old
        } else {
            environment.getPropertySources().addLast(SimpleCommandLinePropertySource(sourceName, arrayOf(*args)))
        }
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
            ApplicationType.MVC -> StandardEnvironment()
        }
    }

    /**
     * 获取SpringApplicationRunListeners
     *
     * @param args 命令行参数列表
     * @return SpringApplicationRunListeners
     */
    protected open fun getRunListeners(args: Array<String>): SpringApplicationRunListeners {
        // 获取所有的SpringApplicationRunListener
        val runListeners = getSpringFactoriesInstances(
            SpringApplicationRunListener::class.java,
            arrayOf(SpringApplication::class.java, Array<String>::class.java),
            this, args
        )
        // 构建成为SpringApplicationRunListeners对象并返回
        return SpringApplicationRunListeners(runListeners, applicationStartup, logger)
    }

    /**
     * 探测SpringApplication的启动类
     */
    private fun deduceMainApplicationClass(): Class<*>? {
        java.lang.RuntimeException().stackTrace.forEach {
            if (it.methodName == "main") {
                return ClassUtils.forName<Any>(it.className)
            }
        }
        return null
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
     *
     * @param bootstrappers Bootstrapper列表
     */
    open fun addBootstrappers(bootstrappers: Collection<Bootstrapper>) {
        this.bootstrappers += bootstrappers
    }

    /**
     * 提供SpringApplication的[ApplicationStartup]的设置，在SpringApplication启动过程当中，会自动将
     * [ApplicationStartup]对象设置到SpringApplication的[ApplicationContext]当中，可以替换自定义的[ApplicationStartup]，
     * 去实现更多相关的自定义功能，比如可以替换一个进行日志的输出的[ApplicationStartup], 这样就可以去记录整个Spring应用的启动信息
     *
     * @param applicationStartup ApplicationStartup
     */
    open fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    /**
     * 获取[ApplicationStartup]
     *
     * @return ApplicationStartup
     */
    open fun getApplicationStartup(): ApplicationStartup = this.applicationStartup

    /**
     * 设置Banner打印模式，Console/Log/No
     *
     * @param mode bannerMode
     */
    open fun setBannerMode(mode: Banner.Mode) {
        this.bannerMode = mode
    }

    /**
     * 获取Banner打印的模式
     *
     * @return bannerMode
     */
    open fun getBannerMode(): Banner.Mode = this.bannerMode

    /**
     * 自定义去进行设置ApplicationType
     *
     * @param applicationType ApplicationType
     */
    open fun setApplicationType(applicationType: ApplicationType) {
        this.applicationType = applicationType
    }

    /**
     * 打印SpringApplication启动过程当中的相关环境信息，只要当前容器是root容器时才需要去进行打印
     *
     * @param isRoot 当前是否是root容器? 只有root容器才会输出
     */
    protected open fun logStartingInfo(isRoot: Boolean) {
        if (isRoot) {
            StartupInfoLogger(this.mainApplicationClass).logStarting(getApplicationLogger())
        }
    }

    /**
     * 日志输出Spring应用启动过程当中的profile信息
     *
     * @param context 正在启动的ApplicationContext
     */
    protected open fun logStartupProfileInfo(context: ConfigurableApplicationContext) {
        val applicationLogger = getApplicationLogger()
        if (applicationLogger.isInfoEnabled) {
            val activeProfiles = quoteProfiles(context.getEnvironment().getActiveProfiles())
            if (activeProfiles.isEmpty()) {
                val defaultProfiles = quoteProfiles(context.getEnvironment().getDefaultProfiles())
                val message = String.format(
                    "%s default %s: ",
                    defaultProfiles.size,
                    if (defaultProfiles.size <= 1) "profile" else "profiles"
                )
                applicationLogger.info(
                    "No active profile set, falling back to $message" + collectionToCommaDelimitedString(defaultProfiles)
                )
            } else {
                val message =
                    if (activeProfiles.size == 1) "1 profile is active: " else activeProfiles.size.toString() + " profiles are active: "
                applicationLogger.info("The following $message" + collectionToCommaDelimitedString(activeProfiles))
            }
        }
    }

    /**
     * 为所有的profile去添加引号
     *
     * @param profiles profile列表
     * @return 转换(添加引号)之后的profile列表
     */
    private fun quoteProfiles(profiles: Array<String>): List<String> = profiles.map { "\"$it\"" }.toList()

    /**
     * 获取当前SpringApplication的Logger，如果有主启动类的话，那么使用该类作为loggerName去进行获取Logger；
     * 如果没有的话，那么使用SpringApplication的Logger去作为Logger
     *
     * @return Application Logger
     */
    open fun getApplicationLogger(): Logger {
        if (this.mainApplicationClass != null) {
            return LoggerFactory.getLogger(this.mainApplicationClass)
        }
        return this.logger
    }

    open fun getMainApplicationClass(): Class<*>? {
        return this.mainApplicationClass
    }

    /**
     * 获取ClassLoader
     *
     * @return 如果ResourceLoader当中可以获取到的话，那么就使用；否则使用默认的
     */
    open fun getClassLoader(): ClassLoader = this.resourceLoader?.getClassLoader() ?: ClassUtils.getDefaultClassLoader()

    /**
     * 设置SpringApplication的ResourceLoader
     *
     * @param resourceLoader 你想要使用的ResourceLoader
     */
    open fun setResourceLoader(resourceLoader: ResourceLoader?) {
        this.resourceLoader = resourceLoader
    }

    /**
     * 获取SpringApplication的ResourceLoader
     *
     * @return ResourceLoader
     */
    open fun getResourceLoader(): ResourceLoader? = this.resourceLoader

    open fun setLogStartupInfo(logStartupInfo: Boolean) {
        this.logStartupInfo = logStartupInfo
    }

    /**
     * 设置SpringApplication的mainClass，不设置的话，可以自动从StackTrace当中去进行推断
     *
     * @param mainApplicationClass 主启动类
     */
    open fun setMainApplicationClass(mainApplicationClass: Class<*>?) {
        this.mainApplicationClass = mainApplicationClass
    }

    /**
     * 自定义SpringApplication的Environment，不自定义的话，支持去进行自动推断
     *
     * @param environment Environment
     */
    open fun setEnvironment(environment: ConfigurableEnvironment) {
        this.environment = environment
    }

    /**
     * 添加一个Source
     *
     * @param sources sources
     */
    open fun addSources(vararg sources: Any) {
        sources.forEach(this.sources::add)
    }

    /**
     * 设置Source
     *
     * @param sources sources
     */
    open fun setSources(sources: Collection<Any>) {
        this.sources = LinkedHashSet(sources)
    }

    /**
     * 添加一个primarySource，它将会作为Spring容器的根启动类
     *
     * @param clazz primarySource
     */
    open fun addPrimarySource(clazz: Class<*>) {
        this.primarySources.add(clazz)
    }

    /**
     * 批量添加primarySources
     *
     * @param sources primarySources
     */
    open fun addPrimarySources(vararg sources: Class<*>) {
        sources.forEach(this::addPrimarySource)
    }

    /**
     * 获取所有的Source列表，包含primarySources和普通的source
     *
     * @return primarySource & source
     */
    open fun getAllSources(): List<Any> {
        val sources: MutableList<Any> = ArrayList(this.primarySources)
        sources.addAll(this.sources)
        return sources
    }

    /**
     * 设置是否允许BeanDefinition的覆盖？
     *
     * @param allowBeanDefinitionOverriding 如果为true，允许覆盖；否则不允许
     */
    open fun setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding: Boolean) {
        this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding
    }

    /**
     * 是否允许BeanDefinition的覆盖？
     *
     * @return 如果允许覆盖return true；否则return false
     */
    open fun isAllowBeanDefinitionOverriding() = this.allowBeanDefinitionOverriding

    /**
     * 设置用于创建ApplicationContext的ApplicationContextClass
     *
     * @param applicationContextClass 你要使用的用来创建ApplicationContext的类
     */
    open fun setApplicationContextClass(applicationContextClass: Class<out ConfigurableApplicationContext>?) {
        this.applicationContextClass = applicationContextClass
    }

    /**
     * 获取用来创建ApplicationContext的类
     *
     * @return 用来创建ApplicationContext的类(没有设置的话，为null)
     */
    open fun getApplicationContextClass(): Class<out ConfigurableApplicationContext>? = this.applicationContextClass

    /**
     * 是否需要添加ConversionService?(默认为true)
     *
     * @return 如果需要添加ConversionService, 那么return true; 否则return false
     */
    open fun isAddConversionService(): Boolean = this.addConversionService

    /**
     * 设置是否需要添加ConversionService(默认为true)
     *
     * @param addConversionService 如果设置为false, 将不会添加ConversionService; 否则将会添加
     */
    open fun setAddConversionService(addConversionService: Boolean) {
        this.addConversionService = addConversionService
    }

    /**
     * 设置自定义的[BeanNameGenerator]
     *
     * @param beanNameGenerator BeanNameGenerator
     */
    fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator
    }

    /**
     * 获取[BeanNameGenerator]
     *
     * @return BeanNameGenerator(如果不存在的话, return null)
     */
    fun getBeanNameGenerator(): BeanNameGenerator? = this.beanNameGenerator
}