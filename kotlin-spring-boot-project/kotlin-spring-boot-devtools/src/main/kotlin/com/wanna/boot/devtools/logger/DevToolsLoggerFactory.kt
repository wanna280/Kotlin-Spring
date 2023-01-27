package com.wanna.boot.devtools.logger

import com.wanna.boot.context.event.ApplicationPreparedEvent
import com.wanna.boot.logging.DeferredLog
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.context.event.ApplicationListener

/**
 * DevTools的[LoggerFactory], 基于[DeferredLog]去进行实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
object DevToolsLoggerFactory {

    /**
     * 维护Logger列表, Key-Logger, Value-该Logger进行输出的source
     */
    @JvmStatic
    private val loggers = LinkedHashMap<Logger, Class<*>>()


    /**
     * getLogger, 获取到DevTools的[DeferredLog]去作为[Logger]进行输出输出
     *
     * @param source source
     * @return Logger
     */
    @JvmStatic
    fun getLogger(source: Class<*>): Logger {
        synchronized(loggers) {
            val logger = DeferredLog()
            loggers[logger] = source
            return logger
        }
    }

    /**
     * 在SpringApplication已经准备好的时候, 将之前保存下来的这些[DeferredLog]日志去进行输出
     */
    class Listener : ApplicationListener<ApplicationPreparedEvent> {
        override fun onApplicationEvent(event: ApplicationPreparedEvent) {
            synchronized(loggers) {
                for ((logger, source) in loggers) {
                    if (logger is DeferredLog) {
                        logger.switchTo(source)
                    }
                }
                loggers.clear()
            }
        }
    }

}