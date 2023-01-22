package com.wanna.boot.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import com.wanna.boot.logging.*
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ResourceUtils
import com.wanna.framework.util.StringUtils
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * 基于Logback日志组件的[LoggingSystem]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param classLoader ClassLoader to use
 */
open class LogbackLoggingSystem(classLoader: ClassLoader) : AbstractLoggingSystem(classLoader) {

    companion object {
        /**
         * 维护LogLevel与Logback的Level之间的映射关系
         */
        @JvmStatic
        private val LEVELS = LogLevels<Level>()

        init {
            LEVELS.map(LogLevel.FATAL, Level.ERROR)
            LEVELS.map(LogLevel.ERROR, Level.ERROR)
            LEVELS.map(LogLevel.WARN, Level.WARN)
            LEVELS.map(LogLevel.INFO, Level.INFO)
            LEVELS.map(LogLevel.DEBUG, Level.DEBUG)
            LEVELS.map(LogLevel.TRACE, Level.ALL)
            LEVELS.map(LogLevel.TRACE, Level.TRACE)
            LEVELS.map(LogLevel.OFF, Level.OFF)
        }
    }

    /**
     * Logback的标准配置文件的位置
     *
     * @return logback的标准配置文件的位置
     */
    override fun getStandardConfigLocations(): Array<String> {
        return arrayOf("logback-test.groovy", "logback-test.xml", "logback.groovy", "logback.xml")
    }

    /**
     * 设置给定loggerName对应的Logger的日志级别
     *
     * @param logLevel system logLevel to set
     * @param loggerName loggerName
     */
    override fun setLogLevel(loggerName: String, @Nullable logLevel: LogLevel?) {
        val logger = getLogger(loggerName)
        if (logger != null) {
            logger.level = LEVELS.convertSystemToNative(logLevel)
        }
    }

    /**
     * 获取Logback的[LoggingSystem]所支持的[LogLevel]
     *
     * @return supported LogLevels
     */
    override fun getSupportedLogLevels(): Set<LogLevel> = LEVELS.getSupported()

    /**
     * 获取Logback的所有的Logger的配置信息
     *
     * @return 所有的Logger的配置信息
     */
    override fun getLoggerConfigurations(): List<LoggerConfiguration> {
        val result = ArrayList<LoggerConfiguration>()
        val loggerContext = getLoggerContext()
        for (logger in loggerContext.loggerList) {
            result.add(getLoggerConfiguration(logger)!!)
        }
        result.sortWith(CONFIGURATION_COMPARATOR) // sort
        return result
    }

    /**
     * 获取到指定的Logger的配置信息
     *
     * @param loggerName loggerName
     * @return 该Logger的配置信息
     */
    @Nullable
    override fun getLoggerConfiguration(loggerName: String): LoggerConfiguration? {
        val name = getLoggerName(loggerName)
        val loggerContext = getLoggerContext()
        return getLoggerConfiguration(loggerContext.exists(name))
    }

    /**
     * 加载SpringBoot对于Logback的自定义的配置信息的加载
     *
     * @param context context
     * @param logFile LogFile
     */
    override fun loadDefaults(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {
        val loggerContext = getLoggerContext()
        val config = LogbackConfigurator(loggerContext)

        // 将Logback的相关系统属性, 去添加到LoggerContext当中去...
        LogbackLoggingSystemProperties(context.environment, loggerContext::putProperty).apply(logFile)

        DefaultLogbackConfiguration(logFile).apply(config)
    }

    /**
     * 执行加载Logback的配置文件
     *
     * @param context context
     * @param configLocation 要去进行加载的配置文件路径
     * @param logFile LogFile(日志文件的路径/文件名)
     */
    override fun loadConfiguration(
        context: LoggingInitializationContext,
        configLocation: String,
        @Nullable logFile: LogFile?
    ) {
        // 将Environment当中对于Logger的相关配置信息, 去应用到SystemProperties当中去...
        applySystemProperties(context.environment, logFile)

        val loggerContext = getLoggerContext()
        // stop and reset Logback LoggerContext
        stopAndReset(loggerContext)
        try {
            configureByResourceUrl(context, loggerContext, ResourceUtils.getURL(configLocation))
        } catch (ex: Exception) {
            throw IllegalStateException("Could not initialize Logback logging from $configLocation", ex)
        }
    }

    /**
     * 关闭之前的Logback的[LoggerContext]并进行重设, 在这里会去reset所有的Logger信息
     *
     * @param loggerContext LoggerContext
     */
    private fun stopAndReset(loggerContext: LoggerContext) {
        loggerContext.stop()
        loggerContext.reset()
    }

    /**
     * 根据配置文件的ResourceURL, 去对[LoggerContext]去进行配置
     *
     * @param context context
     * @param loggerContext LoggerContext
     * @param url Logback的配置文件所在的资源URL
     */
    private fun configureByResourceUrl(context: LoggingInitializationContext, loggerContext: LoggerContext, url: URL) {
        // 如果是XML的话, 那么走SpringBoot自定义的JoranConfigurator
        if (url.toString().endsWith("xml")) {
            val configurator = SpringBootJoranConfigurator(context)
            configurator.context = loggerContext
            configurator.doConfigure(url)

            // 如果不是XML的话, 那么走Logback的ContextInitializer的configure逻辑去丢出来异常...
        } else {
            ContextInitializer(loggerContext).configureByResource(url)
        }
    }

    /**
     * 针对给定的name去为它获取到对应的LoggerName
     *
     * @param name name
     * @return LoggerName
     */
    private fun getLoggerName(name: String): String {
        if (!StringUtils.hasLength(name) || name == ROOT_LOGGER_NAME) {
            return ROOT_LOGGER_NAME
        }
        return name
    }

    /**
     * 获取到给定的Logger的配置信息
     *
     * @param logger Logger
     * @return 该Logger的配置信息
     */
    @Nullable
    private fun getLoggerConfiguration(@Nullable logger: Logger?): LoggerConfiguration? {
        logger ?: return null
        return LoggerConfiguration(
            logger.name, LEVELS.convertNativeToSystem(logger.level),
            LEVELS.convertNativeToSystem(logger.effectiveLevel)
        )
    }

    /**
     * 根据loggerName, 去获取到Logback日志组件的的[Logger]
     *
     * @param loggerName LoggerName
     * @return Logger
     */
    @Nullable
    private fun getLogger(loggerName: String): Logger? {
        val loggerContext = getLoggerContext()
        return loggerContext.getLogger(getLoggerName(loggerName))
    }

    /**
     * 获取Logback日志组件的[LoggerContext]
     *
     * @return LoggerContext
     */
    private fun getLoggerContext(): LoggerContext {
        val factory = LoggerFactory.getILoggerFactory()
        return factory as LoggerContext
    }

    /**
     * Logback的[LoggingSystemFactory]实现
     */
    @Order(Ordered.ORDER_LOWEST)
    class Factory : LoggingSystemFactory {
        companion object {

            /**
             * 检查Logback是否存在?
             */
            @JvmStatic
            private val PRESENT =
                ClassUtils.isPresent("ch.qos.logback.classic.LoggerContext", Factory::class.java.classLoader)
        }

        /**
         * 获取Logback的[LoggingSystem]
         *
         * @param classLoader ClassLoader
         * @return 如果Logback日志组件存在, return [LogbackLoggingSystem]; 否则return null
         */
        @Nullable
        override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem? {
            if (PRESENT) {
                return LogbackLoggingSystem(classLoader)
            }
            return null
        }
    }
}