package com.wanna.boot.logging.java

import com.wanna.boot.logging.*
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ResourceUtils
import com.wanna.framework.util.StringUtils
import java.util.*
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import javax.annotation.Nullable

/**
 * JUL的[LoggingSystem]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @param classLoader ClassLoader to use
 *
 * @see java.util.logging.LogManager
 * @see java.util.logging.Logger
 */
open class JavaLoggingSystem(classLoader: ClassLoader) : AbstractLoggingSystem(classLoader) {

    companion object {
        /**
         * 维护LogLevel与JUL Logger之间的日志级别的Level之间的映射关系
         */
        @JvmStatic
        private val LEVELS = LogLevels<Level>()

        init {
            LEVELS.map(LogLevel.TRACE, Level.FINEST)
            LEVELS.map(LogLevel.DEBUG, Level.FINE)
            LEVELS.map(LogLevel.INFO, Level.INFO)
            LEVELS.map(LogLevel.WARN, Level.WARNING)
            LEVELS.map(LogLevel.ERROR, Level.SEVERE)
            LEVELS.map(LogLevel.FATAL, Level.SEVERE)
            LEVELS.map(LogLevel.OFF, Level.OFF)
        }
    }

    /**
     * 已经配置过日志级别的Loggers
     */
    private val configuredLoggers: MutableSet<Logger> = Collections.synchronizedSet(LinkedHashSet())

    override fun beforeInitialize() {
        super.beforeInitialize()
        Logger.getLogger("").level = Level.SEVERE
    }

    override fun loadDefaults(context: LoggingInitializationContext, @Nullable logFile: LogFile?) {
        if (logFile != null) {
            loadConfiguration(context, getPackagedConfigFile("logging-file.properties"), logFile)
        } else {
            loadConfiguration(context, getPackagedConfigFile("logging.properties"), null)
        }
    }

    /**
     * 加载配置文件
     *
     * @param context context
     * @param configLocation 配置文件路径
     * @param logFile logFile
     */
    override fun loadConfiguration(
        context: LoggingInitializationContext,
        configLocation: String,
        @Nullable logFile: LogFile?
    ) {
        loadConfiguration(configLocation, logFile)
    }

    /**
     * 加载JUL的配置文件
     *
     * @param location 配置文件的资源路径
     * @param logFile LogFile
     */
    protected open fun loadConfiguration(location: String, @Nullable logFile: LogFile?) {
        try {
            var configuration = String(ResourceUtils.getURL(location).openStream().readAllBytes())
            if (logFile != null) {
                configuration = configuration.replace("${'$'}{LOG_PATH}", logFile.toString())
            }
            LogManager.getLogManager().readConfiguration(configuration.byteInputStream())
        } catch (ex: Exception) {
            throw IllegalStateException("Could not initialize Java logging from $location", ex)
        }
    }

    /**
     * 获取到ShutdownHandler, 当VM关闭时会自动触发
     *
     * @return Shutdown Handler
     */
    override fun getShutdownHandler(): Runnable = Runnable { LogManager.getLogManager().reset() }

    /**
     * 获取支持的[LogLevel]
     *
     * @return 支持的LogLevel日志级别列表
     */
    override fun getSupportedLogLevels(): Set<LogLevel> = LEVELS.getSupported()

    /**
     * 设置给定的Logger的日志级别
     *
     * @param loggerName loggerName
     * @param logLevel logLevel
     */
    override fun setLogLevel(loggerName: String, @Nullable logLevel: LogLevel?) {
        var loggerNameToUse = loggerName
        if (loggerName.equals(ROOT_LOGGER_NAME, true)) {
            loggerNameToUse = ""
        }
        val logger = Logger.getLogger(loggerNameToUse)
        if (logger != null) {
            this.configuredLoggers.add(logger)
            logger.level = LEVELS.convertSystemToNative(logLevel)
        }
    }

    /**
     * 获取到JUL的所有的Logger的配置信息
     *
     * @return LoggerConfigurations
     */
    override fun getLoggerConfigurations(): List<LoggerConfiguration> {
        val result = ArrayList<LoggerConfiguration>()
        val loggerNames = LogManager.getLogManager().loggerNames
        for (loggerName in loggerNames) {
            result.add(getLoggerConfiguration(loggerName)!!)
        }
        result.sortWith(CONFIGURATION_COMPARATOR) // sort
        return result
    }

    /**
     * 获取给定的loggerName的配置信息
     *
     * @param loggerName loggerName
     * @return LoggerConfiguration
     */
    @Nullable
    override fun getLoggerConfiguration(loggerName: String): LoggerConfiguration? {
        val logger = Logger.getLogger(loggerName) ?: return null
        val name = if (StringUtils.hasLength(logger.name)) logger.name else ROOT_LOGGER_NAME
        return LoggerConfiguration(
            name,
            LEVELS.convertNativeToSystem(logger.level),
            LEVELS.convertNativeToSystem(getEffectiveLevel(logger))
        )
    }

    /**
     * 获取Logger的有效日志级别
     *
     * @param root Logger
     */
    @Nullable
    private fun getEffectiveLevel(root: Logger): Level? {
        var logger = root
        // 遍历所有的parent, 尝试去进行日志级别的寻找
        while (logger.level == null) {
            logger = logger.parent
        }
        return logger.level
    }

    /**
     * 清理当前[JavaLoggingSystem]
     */
    override fun cleanUp() {
        this.configuredLoggers.clear()
    }

    /**
     * 标准的JUL的配置文件的位置为"logging.properties"
     *
     * @return 标准的JUL的配置文件的位置
     */
    override fun getStandardConfigLocations(): Array<String> = arrayOf("logging.properties")

    @Order(Ordered.ORDER_LOWEST)
    class Factory : LoggingSystemFactory {

        companion object {

            /**
             * JUL是否存在?
             */
            @JvmStatic
            private val PRESENT =
                ClassUtils.isPresent("java.util.logging.LogManager", JavaLoggingSystem::class.java.classLoader)
        }

        /**
         * 如果当前系统当中存在有JUL, 那么返回[JavaLoggingSystem], 否则return null
         *
         * @param classLoader ClassLoader
         * @return JavaLoggingSystem(or null)
         */
        @Nullable
        override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem? {
            if (PRESENT) {
                return JavaLoggingSystem(classLoader)
            }
            return null
        }
    }
}