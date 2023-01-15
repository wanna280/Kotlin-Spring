package com.wanna.logger.impl.utils

import com.wanna.logger.impl.spi.ContextInitializer
import com.wanna.logger.impl.appender.LoggerAppender
import com.wanna.logger.impl.appender.support.ConsoleLoggerAppender
import com.wanna.logger.impl.appender.support.FileLoggerAppender
import com.wanna.logger.impl.appender.support.OutputStreamAppender
import com.wanna.logger.impl.encoder.LoggerEncoder
import com.wanna.logger.impl.encoder.support.LayoutLoggerEncoderBase
import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.layout.LoggerLayout
import com.wanna.logger.impl.layout.support.PatternLayout
import com.wanna.logger.impl.utils.ReflectionUtils.newInstance
import java.util.*

object ConfigurationFileUtils {
    val classLoader: ClassLoader = this::class.java.classLoader

    /**
     * 构建LoggerAppender
     *
     * @param appenderName appenderName
     * @param properties 配置文件当中的相关信息
     * @return 构建好的LoggerAppender, 构建失败return null
     * @throws IllegalArgumentException 如果遇到了其中一项缺失的话
     */
    @Suppress("UNCHECKED_CAST")
    fun buildAppender(appenderName: String, properties: Properties): LoggerAppender {
        // 根据appenderName去获取到Appender的className
        val appenderNameProperty = "${ContextInitializer.LOGGER_APPENDER}.$appenderName"
        val encoderProperty = "$appenderNameProperty.encoder"
        val layoutProperty = "$encoderProperty.layout"
        val layoutEncoderProperty = "$layoutProperty.pattern"

        val appenderClassName = properties[appenderNameProperty]
        val encoderName = properties[encoderProperty]
        val layoutName = properties[layoutProperty]
        val pattern = properties[layoutEncoderProperty]
        /**
         * 关系图如下
         * logger {
         *   appender {
         *      encoder {
         *         layout {
         *             pattern {
         *                  ....
         *             }
         *           }
         *      }
         *   }
         * }
         */
        // 实例化并初始化Appender
        if (appenderClassName is String) {
            val appender: LoggerAppender
            try {
                // 实例化Appender
                appender = newInstance(appenderClassName, classLoader)
            } catch (ex: ClassNotFoundException) {
                throw IllegalArgumentException("指定的Appender[$appenderName]没有找到")
            }
            if (encoderName is String) {
                val encoder: LoggerEncoder<ILoggingEvent>
                try {
                    // 实例化LoggerEncoder
                    encoder = newInstance(encoderName, classLoader)
                } catch (ex: ClassNotFoundException) {
                    throw IllegalArgumentException("指定的Encoder[$encoderName]没有找到")
                }
                if (layoutName is String) {
                    // 实例化Layout
                    val layout: LoggerLayout<ILoggingEvent>
                    try {
                        layout = newInstance(layoutName, classLoader)
                    } catch (ex: ClassNotFoundException) {
                        throw IllegalArgumentException("指定的Layout没有找到[$layoutName]")
                    }
                    if (encoder is LayoutLoggerEncoderBase<*>) {
                        encoder.setLayout(layout)
                    }
                    // 给Layout去设置Pattern
                    if (pattern is String && layout is PatternLayout) {
                        layout.setPattern(pattern)
                    } else {
                        throw IllegalArgumentException("没有为[Layout=$layoutProperty]设置pattern, 请通过[$layoutEncoderProperty]进行设置")
                    }
                } else {
                    throw IllegalArgumentException("指定的Layout[$layoutName]没有找到")
                }
                // 如果appender是一个OutputStream的Appender, 那么需要设置设置到Appender当中
                if (appender is OutputStreamAppender) {
                    appender.encoder = encoder
                }
            } else {
                throw IllegalArgumentException("没有为[Appender=$appenderName]指定Encoder, 请通过[$encoderProperty]进行设置")
            }
            extendsAppender(appender, appenderNameProperty, properties)
            return appender
        } else {
            throw IllegalArgumentException(
                "没有指定Appender[${appenderName}], 请通过[$appenderNameProperty]进行设置"
            )
        }
    }

    /**
     *  扩展Appender
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    private fun extendsAppender(appender: LoggerAppender, appenderNameProperty: String, properties: Properties) {
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
     * 扩展ConsoleAppender, 可以在这里完成OutputStream的设置
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    private fun extendsConsoleAppender(
        appender: ConsoleLoggerAppender, appenderNameProperty: String, properties: Properties
    ) {
        val outputStreamProperty = "$appenderNameProperty.out"
        val outputStream = properties[outputStreamProperty] ?: ContextInitializer.DEFAULT_CONSOLE_OUT
        if (outputStream == ContextInitializer.CONSOLE_OUT_SYSTEM_OUT) {
            appender.out = ConsoleLoggerAppender.systemOut
        } else if (outputStream == ContextInitializer.CONSOLE_OUT_SYSTEM_ERROR) {
            appender.out = ConsoleLoggerAppender.systemError
        }
    }

    /**
     * 扩展FileAppender, 可以在这里完成配置文件的路径的设置
     *
     * @param appender 创建好的Appender
     * @param properties 配置文件的相关信息
     * @param appenderNameProperty appenderName在properties当中的key
     */
    private fun extendsFileAppender(
        appender: FileLoggerAppender, appenderNameProperty: String, properties: Properties
    ) {
        val logFileProperty = "$appenderNameProperty.logfile"
        val logFile = properties[logFileProperty] ?: ContextInitializer.DEFAULT_LOG_FILE_LOCATION
        appender.setLogFilePath(logFile.toString())
    }
}