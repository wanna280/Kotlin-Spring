package com.wanna.logger.impl.spi


import com.wanna.logger.impl.AbstractLoggerContext
import com.wanna.logger.impl.LogcLogger
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.utils.ConfigurationFileUtils.buildAppender
import com.wanna.logger.impl.utils.PropertiesUtils
import com.wanna.logger.impl.utils.ServiceLoaderUtils
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
        // 1.从配置文件加载配置信息Properties
        val properties: Properties = PropertiesUtils.loadProperties(LOGGER_CONFIG_FILE_DEFAULT_LOCATION)

        // 2.beforeApply模板方法，交给子类去进行完成扩展
        beforeApply(properties, this.loggerContext)

        // 3.检查是否有配置文件，如果有Logger配置文件，那么应用配置文件；
        // 如果没有配置文件，那么使用SPI机制去进行加载Configurator，如果也没加载到，那么就应用默认的
        // 如果加载到了，那么就使用SPI机制给定的Configurator去完成LoggerContext的初始化工作
        if (properties.isEmpty) {
            val configurator = ServiceLoaderUtils.loadFirst(Configurator::class.java)
            if (configurator != null) {
                configurator.configure(loggerContext)
            } else {
                BasicConfigurator().configure(this.loggerContext)
            }
        } else {
            applyCustomLogger(properties)
        }

        // 4.afterApply模板方法，交给子类去完成扩展
        afterApply(properties, this.loggerContext)
    }


    /**
     * 根据Properties，去应用在配置文件当中的自定义的RootLogger相关信息
     *
     * @param properties 配置文件当中的相关信息
     */
    protected open fun applyCustomLogger(properties: Properties) {
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
                loggerContext.root.addAppenders(buildAppender(it, properties))
            } catch (ex: Exception) {
                System.err.println("Warning：Appender[$it]初始化失败，原因是：${ex.message}")
            }
        }
    }

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