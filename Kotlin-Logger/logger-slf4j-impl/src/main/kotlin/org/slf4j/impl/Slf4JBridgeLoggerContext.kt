package org.slf4j.impl

import org.slf4j.ILoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个桥接Slf的ILoggerFactory，对子类当中的默认属性去进行扩展
 *
 * @see com.wanna.logger.impl.AbstractLoggerContext
 * @see Slf4jBridgeLogcLogger
 */
open class Slf4JBridgeLoggerContext : ILoggerFactory,
    com.wanna.logger.impl.AbstractLoggerContext<Slf4jBridgeLogcLogger>() {

    companion object {
        const val ROOT_LOGGER_NAME = "root"
    }

    override fun newLoggerCache(): MutableMap<String, Slf4jBridgeLogcLogger> {
        return ConcurrentHashMap<String, Slf4jBridgeLogcLogger>()
    }

    override fun newLogger(): Slf4jBridgeLogcLogger {
        return Slf4jBridgeLogcLogger()
    }

    override fun initRootLogger(): Slf4jBridgeLogcLogger {
        return newLogger(ROOT_LOGGER_NAME)
    }

    override fun newLogger(name: String): Slf4jBridgeLogcLogger {
        return Slf4jBridgeLogcLogger(name)
    }
}