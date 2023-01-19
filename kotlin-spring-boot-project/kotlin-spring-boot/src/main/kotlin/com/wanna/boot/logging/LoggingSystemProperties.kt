package com.wanna.boot.logging

import com.wanna.boot.system.ApplicationPid
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.PropertyResolver
import com.wanna.framework.lang.Nullable
import java.nio.charset.Charset
import java.util.function.BiConsumer

/**
 * 对于[LoggingSystem]的相关配置信息, 主要从[ConfigurableEnvironment]当中去进行提取, 并将其设置到[System.getProperties]当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @param environment Environment
 * @param setter 对于配置的属性值设置的Setter回调函数
 */
open class LoggingSystemProperties(
    private val environment: ConfigurableEnvironment,
    private val setter: BiConsumer<String, String?>
) {
    constructor(environment: ConfigurableEnvironment) : this(environment, systemPropertiesSetter)

    companion object {
        /**
         * 包含了进程的PID的系统属性Key
         */
        const val PID_KEY = "PID"

        /**
         * 包含了"exception conversion word"的系统属性Key
         */
        const val EXCEPTION_CONVERSION_WORD = "LOG_EXCEPTION_CONVERSION_WORD"

        /**
         * 包含了日志文件的系统属性Key
         */
        const val LOG_FILE = "LOG_FILE"

        /**
         * 包含了日志文件的路径的系统属性Key
         */
        const val LOG_PATH = "LOG_PATH"

        /**
         * 命令行的Logger的Pattern的系统属性Key
         */
        const val CONSOLE_LOG_PATTERN = "CONSOLE_LOG_PATTERN"

        /**
         * 命令行的Logger的Charset字符集的系统属性Key
         */
        const val CONSOLE_LOG_CHARSET = "CONSOLE_LOG_CHARSET"

        /**
         * 日志文件的Pattern的系统属性Key
         */
        const val FILE_LOG_PATTERN = "FILE_LOG_PATTERN"

        /**
         * 日志文件的Pattern的系统属性Key
         */
        const val FILE_LOG_CHARSET = "FILE_LOG_CHARSET"

        /**
         * LogLevelPattern的系统属性Key
         */
        const val LOG_LEVEL_PATTERN = "LOG_LEVEL_PATTERN"

        /**
         * Log的日期格式化Pattern的系统属性Key
         */
        const val LOG_DATEFORMAT_PATTERN = "LOG_DATEFORMAT_PATTERN"

        /**
         * SystemProperties的Setter
         */
        @JvmStatic
        private val systemPropertiesSetter = BiConsumer<String, String?> { name, value ->
            if (System.getProperty(name) === null && value != null) {
                System.setProperty(name, value)
            }
        }
    }


    /**
     * 将Environment当中的关于Log的相关配置, 去应用到SystemProperties当中
     *
     * @see System.getProperties
     */
    open fun apply() = apply(null)

    /**
     * 将Environment当中的关于Log的相关配置, 去应用到SystemProperties当中
     *
     * @see System.getProperties
     * @param logFile 要去进行应用的日志文件配置信息
     */
    open fun apply(@Nullable logFile: LogFile?) {
        val propertyResolver = getPropertyResolver()
        apply(logFile, propertyResolver)
    }


    protected open fun apply(@Nullable logFile: LogFile?, propertyResolver: PropertyResolver) {
        setSystemProperty(propertyResolver, EXCEPTION_CONVERSION_WORD, "logging.exception-conversion-word")
        setSystemProperty(propertyResolver, PID_KEY, ApplicationPid().toString())
        setSystemProperty(propertyResolver, CONSOLE_LOG_PATTERN, "logging.pattern.console")
        setSystemProperty(propertyResolver, CONSOLE_LOG_CHARSET, "logging.charset.console", getDefaultCharset().name())
        setSystemProperty(propertyResolver, LOG_DATEFORMAT_PATTERN, "logging.pattern.dateformat")
        setSystemProperty(propertyResolver, FILE_LOG_PATTERN, "logging.pattern.file")
        setSystemProperty(propertyResolver, FILE_LOG_CHARSET, "logging.charset.file", getDefaultCharset().name())
        setSystemProperty(propertyResolver, LOG_LEVEL_PATTERN, "logging.level.pattern")
        logFile?.applyToSystemProperties()
    }

    protected open fun getDefaultCharset(): Charset = Charsets.UTF_8


    /**
     * 获取到提供属性解析的[PropertyResolver]
     *
     * @return PropertyResolver
     */
    private fun getPropertyResolver(): PropertyResolver {
        return this.environment
    }

    protected fun setSystemProperty(
        propertyResolver: PropertyResolver,
        systemPropertyName: String,
        propertyName: String
    ) {
        setSystemProperty(propertyResolver, systemPropertyName, propertyName, null)
    }

    protected fun setSystemProperty(
        propertyResolver: PropertyResolver,
        systemPropertyName: String,
        propertyName: String,
        @Nullable defaultValue: String?
    ) {
        val value = propertyResolver.getProperty(propertyName) ?: defaultValue
        setSystemProperty(systemPropertyName, value)
    }

    protected fun setSystemProperty(name: String, @Nullable value: String?) {
        this.setter.accept(name, value)
    }
}