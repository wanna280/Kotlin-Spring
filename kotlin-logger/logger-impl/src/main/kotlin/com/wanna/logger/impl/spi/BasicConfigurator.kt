package com.wanna.logger.impl.spi

import com.wanna.logger.impl.AbstractLoggerContext
import com.wanna.logger.impl.LogcLogger
import com.wanna.logger.impl.appender.support.ConsoleLoggerAppender
import com.wanna.logger.impl.encoder.support.PatternLayoutEncoder
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.layout.support.PatternLayout

/**
 * 这是一个最基础的Configurator的实现
 *
 * @see Configurator
 */
open class BasicConfigurator : Configurator {

    override fun configure(loggerContext: AbstractLoggerContext<out LogcLogger>) {
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
        loggerContext.root.setLevel(Level.DEBUG)
        val appender = ConsoleLoggerAppender()
        val encoder = PatternLayoutEncoder()

        val layout = PatternLayout()
        layout.setPattern("[%d{yyyy-MM-dd hh:mm:sss}] %green([%p]) --- [%thread] %magenta([%C]) %message %n")
        encoder.setLayout(layout)

        appender.encoder = encoder
        loggerContext.root.addAppenders(appender)
    }
}