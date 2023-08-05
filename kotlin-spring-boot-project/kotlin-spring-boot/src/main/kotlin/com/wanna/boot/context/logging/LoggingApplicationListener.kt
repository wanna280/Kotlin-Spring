package com.wanna.boot.context.logging

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.boot.context.event.ApplicationFailedEvent
import com.wanna.boot.context.event.ApplicationPreparedEvent
import com.wanna.boot.context.event.ApplicationStartingEvent
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.boot.logging.*
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ContextClosedEvent
import com.wanna.framework.context.event.GenericApplicationListener
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import com.wanna.framework.util.StringUtils
import java.util.function.BiConsumer

/**
 * Logging的处理的[GenericApplicationListener], 提供SpringBoot当中对于[LoggingSystem]的自动配置;
 *
 * * 1.可以使用"logging.config=xxx.xml"这样的配置信息, 在配置文件当中去进行自定义配置文件的路径,
 * 如果没有去进行特殊的自定义, 那么将会按照下面的步骤去进行配置文件的探索
 *     * 1.1 尝试使用日志组件默认的配置文件(比如"logback.xml");
 *     * 1.2 尝试使用Spring对于日志组件支持的配置文件(比如"logback-spring.xml");
 *     * 1.3 尝试使用SpringBoot默认的配置文件.
 * * 2.可以使用类似"logging.level.root=INFO"这样的配置信息, 在配置文件当中去进行Logger的日志级别的配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see LoggingSystem
 * @see LoggerGroups
 * @see AbstractLoggingSystem
 */
open class LoggingApplicationListener : GenericApplicationListener {
    companion object {
        /**
         * 当前的LoggingApplicationListener支持去进行处理的事件类型列表
         */
        @JvmStatic
        private val EVENT_TYPES: Array<Class<*>> = arrayOf(
            ApplicationStartingEvent::class.java,
            ApplicationEnvironmentPreparedEvent::class.java,
            ApplicationPreparedEvent::class.java,
            ContextClosedEvent::class.java,
            ApplicationFailedEvent::class.java
        )

        /**
         * 配置LoggingLevel的属性Key前缀, 参考配置"logging.level.root=INFO"
         *
         * @see STRING_LOGLEVEL_MAP
         */
        @JvmStatic
        private val LOGGING_LEVEL = ConfigurationPropertyName.of("logging.level")

        /**
         * 完成对于不同的Logger的LogLevel的绑定的Bindable, "logging.level.root=INFO"
         *
         * @see LOGGING_LEVEL
         */
        @JvmStatic
        private val STRING_LOGLEVEL_MAP = Bindable.mapOf(String::class.java, LogLevel::class.java)

        /**
         * 配置LoggingGroup的属性Key前缀
         *
         * @see LoggerGroups
         */
        @JvmStatic
        private val LOGGING_GROUP = ConfigurationPropertyName.of("logging.group")

        /**
         * 用于去对String->List<String>去进行绑定的Bindable
         */
        @JvmStatic
        private val STRING_STRINGS_MAP: Bindable<Map<String, List<String>>> =
            Bindable.of(
                ResolvableType.forClassWithGenerics(
                    MultiValueMap::class.java, String::class.java, String::class.java
                ).asMap()
            )

        /**
         * 默认的优先级
         */
        private const val DEFAULT_ORDER = Ordered.ORDER_HIGHEST + 20

        /**
         * 自定义日志文件的位置的属性Key
         *
         * @see initializeSystem
         */
        private const val CONFIG_PROPERTY = "logging.config"

        /**
         * 检查是否需要去进行注册Logging的ShutdownHook的属性Key
         *
         * @see LoggingSystem.getShutdownHandler
         */
        private const val REGISTER_SHUTDOWN_HOOK_PROPERTY = "logging.register-shutdown-hook"

        /**
         * 将[LoggingSystem]作为Bean去注册到BeanFactory当中的BeanName
         */
        private const val LOGGING_SYSTEM_BEAN_NAME = "springBootLoggingSystem"

        /**
         * 将[LogFile]作为Bean去注册到BeanFactory当中的BeanName
         */
        private const val LOG_FILE_BEAN_NAME = "springBootLogFile"

        /**
         * 将[LoggerGroups]作为Bean去注册到BeanFactory当中的BeanName
         */
        private const val LOGGER_GROUPS_BEAN_NAME = "springBootLoggerGroups"

        /**
         * 将Logging的Lifecycle注册到BeanFactory当中的BeanName
         */
        private const val LOGGING_LIFECYCLE_BEAN_NAME = "springBootLoggingLifecycle"

        /**
         * 默认分组的Loggers(Key-Logger所在分组, Value-该分组所包含的Loggers, 实现对于一个分组的日志级别去进行配置, 即可对于多个包下的日志级别去进行同时配置);
         *
         * 例如可以通过"logging.level.web"去配置web分组对应的Logger的日志级别
         * @see LoggerGroups
         */
        @JvmStatic
        private val DEFAULT_GROUP_LOGGERS: Map<String, List<String>>

        /**
         * SpringBoot的Logger的日志级别配置
         *
         * 可以通过"--debug"/"--trace"命令行参数, 去激活对应的包下的日志级别的配置
         */
        @JvmStatic
        private val SPRING_BOOT_LOGGING_LOGGERS: Map<LogLevel, List<String>>

        init {
            val defaultLoggers = LinkedMultiValueMap<String, String>()
            defaultLoggers.add("web", "com.wanna.boot.web")
            defaultLoggers.add("web", "com.wanna.framework.web")
            defaultLoggers.add("web", "com.wanna.boot.actuate.web")
            defaultLoggers.add("sql", "org.hibernate.SQL")
            this.DEFAULT_GROUP_LOGGERS = defaultLoggers

            val loggers = LinkedMultiValueMap<LogLevel, String>()

            // 对于"sql"/"web"等相关, 使用DEBUG级别
            loggers.add(LogLevel.DEBUG, "sql")
            loggers.add(LogLevel.DEBUG, "web")

            // boot也使用DEBUG级别
            loggers.add(LogLevel.DEBUG, "com.wanna.boot")

            // 对于tomcat/framework, 使用TRACE级别
            loggers.add(LogLevel.TRACE, "com.wanna.framework")
            loggers.add(LogLevel.TRACE, "org.apache.catalina")
            loggers.add(LogLevel.TRACE, "org.apache.tomcat")
            this.SPRING_BOOT_LOGGING_LOGGERS = loggers
        }
    }

    /***
     * 是否需要去解析"debug"/"trace"系统属性去作为早期的日志级别? 默认为true
     * (通常是被"--debug"/"--trace"的方式去配置在命令行参数当中)
     */
    private var parseArgs = true

    /**
     * 是否需要为SpringBoot以及相关依赖组件的包, 去设置自定义的日志级别?
     *
     * @see SPRING_BOOT_LOGGING_LOGGERS
     */
    @Nullable
    private var springBootLogging: LogLevel? = null

    /**
     * LoggingSystem, 各个日志组件(log4j2/logback/jul等)针对[LoggingSystem]提供不同的实现
     */
    @Nullable
    private var loggingSystem: LoggingSystem? = null

    /**
     * LogFile, 维护日志组件的日志文件名/日志文件路径
     */
    @Nullable
    private var logFile: LogFile? = null

    /**
     * LoggerGroups, 对于Logger的分组, 可以基于"logging.level"配置, 去实现对于不同的包的配置去进行分组,
     * 例如在配置文件当中去进行下面这样的配置"logging.group.wanna=com.wanna.controller,com.wanna.user",
     * 那么就代表了将"com.wanna.controller"和"com.wanna.user"都去划分到"wanna"分组下,
     * 就可以使用"logging.level.wanna=INFO"这样的配置, 去实现将"com.wanna.controller"和"com.wanna.user"的日志级别的配置
     *
     * @see LOGGING_GROUP
     */
    @Nullable
    private var loggerGroups: LoggerGroups? = null

    /**
     * 当前ApplicationListener的优先级
     */
    private var order = DEFAULT_ORDER

    /**
     * 执行处理[ApplicationEvent]事件
     *
     * @param event event
     */
    override fun onApplicationEvent(event: ApplicationEvent) {
        when (event) {
            // 在SpringApplication开始启动时, 需要去完成LoggingSystem的预先初始化
            is ApplicationStartingEvent -> onApplicationStartingEvent(event)

            // 在SpringApplication的Environment已经准备好时, 需要根据Environment去完成LoggingSystem的初始化
            is ApplicationEnvironmentPreparedEvent -> onApplicationEnvironmentPreparedEvent(event)

            // 在SpringApplication的ApplicationContext已经准备好时, 需要将LoggingSystem的相关Bean去注册到BeanFactory当中
            is ApplicationPreparedEvent -> onApplicationPreparedEvent(event)

            // 在SpringApplication的ApplicationContext关闭时, 需要尝试去进行LoggingSystem的清理...
            is ContextClosedEvent -> onContextClosedEvent(event)

            // 在SpringApplication的ApplicationContext启动失败时, 需要直接去完成LoggingSystem的清理...
            is ApplicationFailedEvent -> onApplicationFailedEvent(event)
        }
    }

    /**
     * 当Spring Application开始启动时, 先构建出来[LoggingSystem]并完成预初始化
     *
     * @param event event
     */
    private fun onApplicationStartingEvent(event: ApplicationStartingEvent) {
        val loggingSystem = LoggingSystem.get(event.application.getClassLoader())
        loggingSystem.beforeInitialize()
        this.loggingSystem = loggingSystem
    }

    /**
     * 当Spring Application的Environment准备好时, 此时就可以根据配置信息去完成[LoggingSystem]的初始化
     *
     * @param event event
     */
    private fun onApplicationEnvironmentPreparedEvent(event: ApplicationEnvironmentPreparedEvent) {
        if (this.loggingSystem == null) {
            this.loggingSystem = LoggingSystem.get(event.application.getClassLoader())
        }

        // 完成LoggingSystem的初始化
        initialize(event.environment, event.application.getClassLoader())
    }

    /**
     * 当[ApplicationContext]准备好时, 需要去将[LoggingSystem]用到的相关Bean, 去注册到BeanFactory当中
     *
     * @param event event
     */
    private fun onApplicationPreparedEvent(event: ApplicationPreparedEvent) {
        val applicationContext = event.context
        val beanFactory = event.context.getBeanFactory()

        // 注册LoggingSystem
        if (!beanFactory.containsBean(LOGGING_SYSTEM_BEAN_NAME) && this.loggingSystem != null) {
            beanFactory.registerSingleton(LOGGING_SYSTEM_BEAN_NAME, this.loggingSystem!!)
        }

        // 注册LogFile
        if (!beanFactory.containsBean(LOG_FILE_BEAN_NAME) && this.logFile != null) {
            beanFactory.registerSingleton(LOG_FILE_BEAN_NAME, this.logFile!!)
        }

        // 注册LoggerGroups
        if (!beanFactory.containsBean(LOGGER_GROUPS_BEAN_NAME) && this.loggerGroups != null) {
            beanFactory.registerSingleton(LOGGER_GROUPS_BEAN_NAME, this.loggerGroups!!)
        }

        // 注册Logger Lifecycle
        if (!beanFactory.containsBean(LOGGING_LIFECYCLE_BEAN_NAME) && applicationContext.getParent() == null) {
            beanFactory.registerSingleton(LOGGING_LIFECYCLE_BEAN_NAME, Lifecycle())
        }
    }

    /**
     * 在Spring Application的[Environment]已经彻底准备好时, 可以对[LoggingSystem]去完成彻底的初始化
     *
     * @param environment Environment
     * @param classLoader ClassLoader to use
     */
    protected open fun initialize(environment: ConfigurableEnvironment, classLoader: ClassLoader) {
        getLoggingSystemProperties(environment).apply()

        // 获取到LogFile(filePath/fileName)的配置信息, 并把它apply给SystemProperties当中
        // 可以通过属性"logging.file.name"/去配置fileName, 通过"logging.file.path"属性去配置filePath
        this.logFile = LogFile.get(environment)

        // 将fileName和filePath, 去放入到SystemProperties系统属性当中
        this.logFile?.applyToSystemProperties()

        // 为默认的日志包去进行分组(web/sql)
        this.loggerGroups = LoggerGroups(DEFAULT_GROUP_LOGGERS)

        val loggingSystem = loggingSystem ?: throw IllegalStateException("LoggingSystem is not available")

        // 先去初始化早期的LoggingLevel(检查"--debug"/"--trace"命令行参数, 去初始化早期的SpringBoot相关依赖的日志级别)
        initializeEarlyLoggingLevel(environment)

        // 初始化当前LoggingSystem,
        // 将日志组件对应的配置文件(比如logback的"logback.xml")的相关配置信息都去应用给当前的LoggingSystem...
        initializeSystem(environment, loggingSystem, logFile)

        // 在加载完成配置文件之后, 我们就可以根据配置文件当中的"logging.level"相关的配置信息去初始化最终的LoggingLevel...
        // 例如可以通过"logging.level.ROOT=INFO"去将ROOT Logger的日志级别去配置成为INFO级别...
        initializeFinalLoggingLevels(environment, loggingSystem)

        // 如果必要的话, 注册Logging的Shutdown Hook...
        registerShutdownHookIfNecessary(loggingSystem, environment)
    }

    /**
     * 如果必要的话, 那么为[LoggingSystem]去注册一个ShutdownHook
     *
     * @param loggingSystem LoggingSystem
     * @param environment Environment
     */
    private fun registerShutdownHookIfNecessary(loggingSystem: LoggingSystem, environment: ConfigurableEnvironment) {
        if (environment.getProperty(REGISTER_SHUTDOWN_HOOK_PROPERTY, Boolean::class.java, true)) {
            val shutdownHandler = loggingSystem.getShutdownHandler()
            // TODO ShutdownHook to SpringApplication
            if (shutdownHandler != null) {
                Runtime.getRuntime().addShutdownHook(Thread(shutdownHandler))
            }
        }
    }

    /**
     * 执行初始化日志组件的最终的Logger的LoggingLevel
     *
     * @param environment Environment
     * @param loggingSystem 要去进行初始化的LoggingSystem
     */
    private fun initializeFinalLoggingLevels(environment: ConfigurableEnvironment, loggingSystem: LoggingSystem) {
        // 完成对于LoggingGroups的绑定, 通过"logging.group"去实现对于Logger去进行分组
        bindLoggerGroups(environment)

        // 初始化SpringBoot的依赖包的日志级别配置(通过"--debug"/"--trace"去进行指定要生效的日志级别)
        initializeSpringBootLogging(loggingSystem, springBootLogging)

        // 将"logging.level"相关的属性, 去应用到对应的日志组件的Logger当中
        setLogLevels(loggingSystem, environment)
    }

    /**
     * 将"logging.group"相关的配置, 去绑定到[LoggerGroups]当中, 实现对于Logger的分组
     *
     * @param environment Environment
     */
    private fun bindLoggerGroups(environment: ConfigurableEnvironment) {
        loggerGroups ?: return
        Binder.get(environment).bind(LOGGING_GROUP, STRING_STRINGS_MAP)
            .ifBound(this.loggerGroups!!::putAll)
    }

    /**
     * 执行对于SpringBoot默认的包当中的Logging日志级别去完成初始化
     *
     * @param loggingSystem LoggingSystem
     * @param springBootLogging 要去进行激活的SpringBoot依赖包的日志级别
     */
    protected open fun initializeSpringBootLogging(
        loggingSystem: LoggingSystem,
        @Nullable springBootLogging: LogLevel?
    ) {
        springBootLogging ?: return
        val logLevelConfigurer = getLogLevelConfigurer(loggingSystem)

        // 对SpringBoot的相关依赖, 去进行日志级别的配置...
        SPRING_BOOT_LOGGING_LOGGERS.getOrDefault(springBootLogging, emptyList())
            .forEach { configureLogLevel(it, springBootLogging, logLevelConfigurer) }
    }

    /**
     * 将[Environment]当中的类似"logging.level.root=INFO"的配置信息去绑定到[LoggingSystem]当中, 应用给日志组件的对应的Logger
     *
     * @param loggingSystem LoggingSystem
     * @param environment Environment
     */
    protected open fun setLogLevels(loggingSystem: LoggingSystem, environment: ConfigurableEnvironment) {
        // 获取到对应LogLevel去进行自定义的Callback回调函数, 执行LoggingSystem.setLogLevel
        val logLevelConfigurer = getLogLevelConfigurer(loggingSystem)

        // 完成LogLevel的属性值的绑定
        val loggingLevels = Binder.get(environment).bind(LOGGING_LEVEL, STRING_LOGLEVEL_MAP).orElse(emptyMap())!!

        // 将配置文件当中的配置的Logger对应的日志级别, 都去进行apply到LoggingSystem当中
        loggingLevels.forEach { configureLogLevel(it.key, it.value, logLevelConfigurer) }
    }

    /**
     * 对于给定的loggerName/logger分组groupName对应的Logger, 去进行LogLevel日志级别的配置
     *
     * @param name loggerName/logger分组groupName
     * @param logLevel 要去进行使用的logLevel
     * @param customizer 执行对于Logger的日志级别的的回调函数(第一个参数LoggerName, 第二个参数是该Logger需要设置的日志级别)
     */
    private fun configureLogLevel(name: String, logLevel: LogLevel, customizer: BiConsumer<String, LogLevel?>) {
        // 如果存在有相关的Logger分组, 那么对该分组下的Logger的日志级别去进行批量配置
        if (this.loggerGroups != null) {
            val loggerGroup = this.loggerGroups!!.get(name)
            if (loggerGroup != null && loggerGroup.hasMember()) {
                loggerGroup.configureLogLevel(logLevel, customizer)
                return
            }
        }

        // 如果不存在对应的LoggerGroup, 那么对单个Logger去进行配置...
        customizer.accept(name, logLevel)
    }

    /**
     * 获取到执行对于[LoggingSystem]当中的Logger的LogLevel去进行配置的Callback回调方法
     *
     * @param loggingSystem LoggingSystem
     * @return 执行对于[LoggingSystem]的日志级别的配置的Callback回调函数(第一个参数为LoggerName, 第二个参数为LogLevel)
     */
    private fun getLogLevelConfigurer(loggingSystem: LoggingSystem): BiConsumer<String, LogLevel?> {
        return BiConsumer { loggerName, logLevel ->
            val name = if (LoggingSystem.ROOT_LOGGER_NAME.equals(loggerName, true)) "" else loggerName
            loggingSystem.setLogLevel(name, logLevel)
        }
    }

    /**
     * 根据SpringApplication的[Environment]当中的相关配置信息, 去完成[LoggingSystem]的初始化
     *
     * @param environment Environment
     * @param loggingSystem LoggingSystem
     * @param logFile LogFile
     */
    private fun initializeSystem(
        environment: ConfigurableEnvironment,
        loggingSystem: LoggingSystem,
        @Nullable logFile: LogFile?
    ) {
        val logConfig = environment.getProperty(CONFIG_PROPERTY)
        try {
            val context = LoggingInitializationContext(environment)

            // 如果不存在有指定的配置文件, 那么走没有配置文件的初始化...
            if (ignoreLogConfig(logConfig)) {
                loggingSystem.initialize(context, null, logFile)

                // 如果存在有指定的日志文件, 那么走有日志文件的初始化...
            } else {
                loggingSystem.initialize(context, logConfig, logFile)
            }
        } catch (ex: Exception) {
            // TODO report ex
            throw IllegalStateException(ex)
        }
    }

    /**
     * 检查给定的配置文件名是否需要被忽略?
     *
     * @param logConfig logConfig
     * @return 如果值为null/"", 或者是以"-D"作为开头, 那么应该被忽略, return true; 否则return false
     */
    private fun ignoreLogConfig(@Nullable logConfig: String?): Boolean {
        return !StringUtils.hasLength(logConfig) || logConfig!!.startsWith("-D")
    }

    /**
     * 初始化SpringBoot的早期的日志级别
     *
     * @param environment Environment
     */
    private fun initializeEarlyLoggingLevel(environment: ConfigurableEnvironment) {
        // 解析是否有"debug"/"trace"相关的配置信息? 如果有的话, 将对应的日志级别打开...
        if (this.parseArgs && this.springBootLogging == null) {
            if (isSet(environment, "debug")) {
                springBootLogging = LogLevel.DEBUG
            }
            if (isSet(environment, "trace")) {
                springBootLogging = LogLevel.TRACE
            }
        }
    }

    /**
     * 检查[Environment]当中是否存在有给定的属性Key对应的配置信息
     *
     * @param environment Environment
     * @param property 属性Key
     * @return 如果包含这样的配置, 那么return true; 否则return false
     */
    private fun isSet(environment: ConfigurableEnvironment, property: String): Boolean {
        val propertyValue = environment.getProperty(property)
        return propertyValue != null && propertyValue != "false"
    }

    /**
     * 获取到[LoggingSystem]的Properties配置信息
     *
     * @param environment Environment
     * @return LoggingSystem的配置信息
     */
    private fun getLoggingSystemProperties(environment: ConfigurableEnvironment): LoggingSystemProperties {
        return if (loggingSystem != null) loggingSystem!!.getSystemProperties(environment)
        else LoggingSystemProperties(environment)
    }

    /**
     * 检查当前的ApplicationListener, 是否支持去进行处理当前事件类型?
     *
     * @param type eventType
     *  @return 如果支持去进行处理当前事件类型的话, 那么return true; 否则return false
     */
    override fun supportsEventType(type: ResolvableType): Boolean {
        return isAssignableFrom(type.getRawClass(), EVENT_TYPES)
    }

    /**
     * 检查当前的ApplicationListener, 是否支持去进行处理当前事件类型?
     *
     * @param type 当前事件类型
     * @param supportedTypes 当前ApplicationListener支持去进行处理的事件类型列表
     * @return 如果支持去进行处理当前事件类型的话, 那么return true; 否则return false
     */
    private fun isAssignableFrom(@Nullable type: Class<*>?, supportedTypes: Array<Class<*>>): Boolean {
        if (type != null) {
            for (supportedType in supportedTypes) {
                if (ClassUtils.isAssignFrom(supportedType, type)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 当Spring应用关闭时, 可能需要去清理[LoggingSystem]
     *
     * @param event event
     */
    private fun onContextClosedEvent(event: ContextClosedEvent) {
        val applicationContext = event.applicationContext
        // 如果当前不是root Application, 或者是存在有Lifecycle的Bean, 那么无需我们去进行清理...直接pass
        if (applicationContext.getParent() != null || applicationContext.containsBean(LOGGING_LIFECYCLE_BEAN_NAME)) {
            return
        }

        // 如果当前是rootApplication, 才需要去清理LoggingSystem
        cleanupLoggingSystem()
    }

    /**
     * 当Spring Application启动失败, 那么直接去清理[LoggingSystem]
     *
     * @param event event
     */
    private fun onApplicationFailedEvent(event: ApplicationFailedEvent) {
        cleanupLoggingSystem()
    }

    /**
     * 执行对于[LoggingSystem]的清理
     *
     * @see LoggingSystem.cleanUp
     */
    private fun cleanupLoggingSystem() {
        this.loggingSystem?.cleanUp()
    }

    /**
     * 为SpringBoot以及相关依赖组件的包, 去设置自定义的日志级别
     *
     * @param springBootLogging SpringBootLogging Level
     */
    open fun setSpringBootLogging(springBootLogging: LogLevel) {
        this.springBootLogging = springBootLogging
    }

    /**
     * 是否需要去解析"debug"/"trace"系统属性去作为早期的日志级别? 默认为true
     * (通常是被"--debug"/"--trace"的方式去配置在命令行参数当中)
     *
     * @param parseArgs should parse Args?
     */
    open fun setParseArgs(parseArgs: Boolean) {
        this.parseArgs = parseArgs
    }

    /**
     * 当前ApplicationListener的order
     *
     * @return order
     */
    override fun getOrder(): Int = this.order

    /**
     * 设置当前ApplicationListener的优先级
     *
     * @param order order
     */
    open fun setOrder(order: Int) {
        this.order = order
    }

    private inner class Lifecycle : com.wanna.framework.context.Lifecycle {

        @Volatile
        private var running = false
        override fun start() {
            running = true
        }

        override fun stop() {
            running = false
            cleanupLoggingSystem()
        }

        override fun isRunning(): Boolean = running
    }
}