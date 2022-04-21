package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.api.Logger

/**
 * 这是实现方对于LoggerFactory的具体实现
 */
open class LoggerContext : ILoggerFactory {
    // RootLogger，全局默认的Logger
    var root: Logger? = null

    // 这是Logger的缓存，缓存已经注册过的所有Logger
    private val loggerCache = HashMap<String, Logger>()

    fun addLogger(name: String, logger: Logger) {
        synchronized(this) {
            loggerCache[name] = logger
        }
    }

    fun addLogger(logger: Logger) {
        synchronized(this) {
            loggerCache[logger.getLoggerName()] = logger
        }
    }

    override fun getLogger(name: String): Logger {
        var logger = loggerCache[name]
        if (logger == null) {
            logger = newLogger(name)
        }
        return logger
    }

    override fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.name)
    }

    override fun newLogger(name: String): Logger {
        // 创建一个新的Logger，并设置loggerName
        val logger = LogcLogger()
        logger.setLoggerName(name)

        var parent: Logger? = null
        var loggerName = name
        var index = loggerName.lastIndexOf('.')

        // 遍历它的所有子包名，去判断是否有合适的Logger去负责？
        // 如果在对应的包名下配置了Logger，那么将会采用配置的Logger
        while (index > -1) {
            parent = loggerCache[loggerName]
            if (parent != null) {
                break
            }
            loggerName = loggerName.substring(0, index)
            index = loggerName.lastIndexOf('.')
        }

        // 如果遍历了所有的子包名，都没有找到合适的Logger，那么将会采用全局的RootLogger作为Logger
        if (parent == null) {
            parent = this.root
        }

        logger.setParent(parent!!)
        logger.setLoggerContext(this)
        return logger
    }


}