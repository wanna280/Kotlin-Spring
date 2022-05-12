package com.wanna.logger.impl


import com.wanna.logger.impl.appender.LoggerAppender
import com.wanna.logger.impl.appender.support.ConsoleLoggerAppender
import com.wanna.logger.impl.appender.support.FileLoggerAppender
import com.wanna.logger.impl.appender.support.OutputStreamAppender
import com.wanna.logger.impl.encoder.LoggerEncoder
import com.wanna.logger.impl.encoder.support.PatternLayoutEncoder
import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.utils.PropertiesUtils
import com.wanna.logger.impl.utils.StringUtils
import java.util.*

/**
 * 这是一个Logger的上下文初始化器，去对LoggerContext去完成初始化，在这里可以去完成配置文件的加载，从而去实现对于整个Logger的配置
 */
open class ContextInitializer<T : LogcLogger>(private val loggerContext: AbstractLoggerContext<T>) {
    companion object {
        val DEFAULT_LOGGING_LEVEL = Level.DEBUG
        private const val LOGGER_PREFIX = "logger"
        const val LOGGER_CONFIG_FILE_DEFAULT_LOCATION = "logger.properties"
        const val ROOT_LOGGER_PREFIX = "$LOGGER_PREFIX.root"
        const val ROOT_LOGGER_APPENDER = "$ROOT_LOGGER_PREFIX.appender"
        const val LOGGER_APPENDER = "$LOGGER_PREFIX.appender"

        const val DEFAULT_LOG_FILE_LOCATION = "log.log"

        const val CONSOLE_OUT_SYSTEM_OUT = "System.out"
        const val CONSOLE_OUT_SYSTEM_ERROR = "System.err"
        const val DEFAULT_CONSOLE_OUT = CONSOLE_OUT_SYSTEM_OUT
    }

    /**
     *  logger {
     *      appender: {
     *        encoder: {
     *          pattern: {
     *              .......
     *          }
     *        }
     *      }
     *  }
     */

    /**
     * 需要在这里去完成自动配置，完成配置文件的加载，并实现对LoggerContext的配置工作
     */
    open fun autoConfig() {
        // 1.从配置文件加载Properties
        val properties: Properties = PropertiesUtils.loadProperties(LOGGER_CONFIG_FILE_DEFAULT_LOCATION)

        // 2.beforeApply模板方法，交给子类去进行完成扩展
        beforeApply(properties, this.loggerContext)

        // 3.检查是否配置了RootAppender，并应用合适的RootLogger
        // 如果没有配置root的话，那么使用默认的RootLogger
        // 如果配置了自定义的root的话，那么使用给定的RootLogger的配置
        val rootAppenders = properties[ROOT_LOGGER_APPENDER]

        if (rootAppenders == null) {
            applyDefaultRootLogger()
        } else {
            applyCustomRootLogger(properties)
        }

        // 4.afterApply模板方法，交给子类去完成扩展
        afterApply(properties, this.loggerContext)
    }


    /**
     * 根据Properties，去应用自定义的RootLogger相关信息
     *
     * @param properties 配置文件当中的相关信息
     */
    protected open fun applyCustomRootLogger(properties: Properties) {
        // 从配置文件当中读取并设置RootLogger的LoggingLevel
        val levelStr = properties["$ROOT_LOGGER_PREFIX.level"] ?: DEFAULT_LOGGING_LEVEL.name
        val level = Level.valueOf(levelStr.toString())
        loggerContext.root.setLevel(level)

        // 从配置文件当中获取到对于RootLogger配置的Appender列表，并对每个LoggerAppender去进行相关的配置工作
        val rootAppenders = properties[ROOT_LOGGER_APPENDER]
        val appenderNames = StringUtils.commaDelimitedListToStringArray(rootAppenders.toString())

        // 获取配置文件当中配置的，所有针对于RootLogger所配置的Appender，去进行逐一地构建，并将其应用给RootLogger
        appenderNames.forEach {
            try {
                val appender = buildAppender(it, properties)
                if (appender != null) {
                    loggerContext.root.addAppenders(appender)
                }
            } catch (ignored: Exception) {

            }
        }
    }

    /**
     * 构建LoogerAppender
     *
     * @param appenderName appenderName
     * @param properties 配置文件当中的相关信息
     * @return 构建好的LoggerAppender，构建失败return null
     */
    @Suppress("UNCHECKED_CAST")
    open fun buildAppender(appenderName: String, properties: Properties): LoggerAppender? {

        // 根据appenderName去获取到Appender的className
        val appenderNameProperty = "$LOGGER_APPENDER.$appenderName"
        val appenderClassName = properties[appenderNameProperty]
        if (appenderClassName != null) {

            // 根据获取到的appenderName去获取到Class对象，并使用无参数构造器去完成实例化
            val clazz = Class.forName(
                appenderClassName.toString(), false, ContextInitializer::class.java.classLoader
            ) as Class<out LoggerAppender>
            val appender = clazz.getDeclaredConstructor().newInstance()

            // 一个LoggerAppender下的LoggerEncoder
            val encoderProperty = "$appenderNameProperty.encoder"
            val encoderPatternProperty = "$encoderProperty.pattern"

            val encoderName = properties[encoderProperty]
            if (encoderName != null) {

                // 获取到LoggerEncoder的className，并使用无参数构造器去完成实例化
                val encoderClassName = encoderName.toString()
                val encoder = Class.forName(
                    encoderClassName, false, ContextInitializer::class.java.classLoader
                ).getDeclaredConstructor().newInstance()

                // 如果appender是一个OutputStream的Appender，那么需要设置设置到Appender当中
                if (appender is OutputStreamAppender) {
                    appender.encoder = encoder as LoggerEncoder<ILoggingEvent>
                }

                // 如果LoggerEncoder还是一个PatternLayoutEncoder，那么需要获取到日志格式化的模式(pattern)
                if (encoder is PatternLayoutEncoder) {
                    val pattern = properties[encoderPatternProperty]
                    if (pattern != null) {
                        encoder.setPattern(pattern.toString())
                    }
                }
            }
            extendsAppender(appender, appenderNameProperty, properties)
            return appender
        }
        return null
    }

    /**
     * 自定义扩展Appender的逻辑
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    protected open fun extendsAppender(appender: LoggerAppender, appenderNameProperty: String, properties: Properties) {
        // 如果它是FileAppender的话
        if (appender is FileLoggerAppender) {
            extendsFileAppender(appender, appenderNameProperty, properties)
        }
        // 如果它是ConsoleAppender的话
        if (appender is ConsoleLoggerAppender) {
            extendsConsoleAppender(appender, appenderNameProperty, properties)
        }
    }

    /**
     * 扩展ConsoleAppender，可以在这里完成OutputStream的设置
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    protected open fun extendsConsoleAppender(
        appender: ConsoleLoggerAppender, appenderNameProperty: String, properties: Properties
    ) {
        val outputStreamProperty = "$appenderNameProperty.out"
        val outputStream = properties[outputStreamProperty] ?: DEFAULT_CONSOLE_OUT
        if (outputStream == CONSOLE_OUT_SYSTEM_OUT) {
            appender.out = ConsoleLoggerAppender.systemOut
        } else if (outputStream == CONSOLE_OUT_SYSTEM_ERROR) {
            appender.out = ConsoleLoggerAppender.systemError
        }
    }

    /**
     * 扩展FileAppender，可以在这里完成配置文件的路径的设置
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    protected open fun extendsFileAppender(
        appender: FileLoggerAppender, appenderNameProperty: String, properties: Properties
    ) {
        val logFileProperty = "$appenderNameProperty.logfile"
        val logFile = properties[logFileProperty] ?: DEFAULT_LOG_FILE_LOCATION
        appender.setLogFilePath(logFile.toString())
    }

    /**
     * 应用默认的RootLogger，设置日志级别为DEBUG，并添加ConsoleAppender
     */
    protected open fun applyDefaultRootLogger() {
        loggerContext.root.setLevel(Level.DEBUG)
        loggerContext.root.addAppenders(ConsoleLoggerAppender())
    }


    open fun getLoggerContext(): AbstractLoggerContext<T> = this.loggerContext

    /**
     * apply之前的回调扩展方法
     *
     * @param properties 配置文件
     * @param context LoggerContext相关信息
     */
    protected open fun beforeApply(properties: Properties, context: AbstractLoggerContext<T>) {

    }

    /**
     * apply之后的回调扩展方法
     *
     * @param properties 配置文件
     * @param context LoggerContext相关信息
     */
    protected open fun afterApply(properties: Properties, context: AbstractLoggerContext<T>) {

    }
}