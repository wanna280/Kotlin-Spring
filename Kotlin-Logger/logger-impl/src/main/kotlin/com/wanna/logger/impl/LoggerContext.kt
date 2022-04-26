package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.api.Logger
import com.wanna.logger.impl.filter.LoggerFilterList
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是实现方对于LoggerFactory的具体实现，实现方必须能遵循的API规范，因为API规范的指定方，会用到ILoggerFactory，去进行getLogger
 */
class LoggerContext : ILoggerFactory {
    // RootLogger，全局默认的Logger
    var root: LogcLogger = LogcLogger(LogcLogger.ROOT_LOGGER_NAME)

    // Filter列表
    val filterList: LoggerFilterList = LoggerFilterList()

    init {
        root.setLoggerContext(this)
    }

    // 这是Logger的缓存，缓存已经注册过的所有Logger，为了保证线程安全，采用ConcurrentHashMap
    private val loggerCache = ConcurrentHashMap<String, LogcLogger>()

    override fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.name)
    }

    override fun getLogger(name: String): Logger {
        // 如果name给的是ROOT那么直接return，避免因为获取Logger导致加锁
        if (name == LogcLogger.ROOT_LOGGER_NAME) {
            return root
        }
        // 如果给定name不是ROOT的话，那么确实得尝试从ConcurrentHashMap当中获取了
        var childLogger: LogcLogger? = loggerCache[name]
        if (childLogger != null) {
            return childLogger
        }
        var logger = root
        var index = 0

        // childName记录的是子包名，比如com.wanna.test.TestMain，第一次获取的childName=com，第二次获取的childName=com.wanna
        // 第三次获取的childName=com.wanna.test，以此类推...
        // 如果childName并未在Logger的children当中的话，为childName创建Logger并加入到缓存当中
        // 如果childName已经在Logger的children当中的话，很可能就是之前已经完成过Logger的注册的情况了
        while (true) {
            // 获取name的的"."的index
            val firstIndex = getFirstIndexOf(name, index)
            // 获取子包名，为所有子包创建Logger
            val childName = if (firstIndex == -1) name else name.substring(0, firstIndex)

            // startIndex=firstIndex+1，跳过当前位置的"."，为了下次startIndex能直接从".之后"去进行统计
            index = firstIndex + 1

            // 对Logger去进行加锁
            synchronized(logger) {
                childLogger = logger.getChildByName(childName)
                if (childLogger == null) {
                    childLogger = logger.createChildByName(childName)
                }
                loggerCache[childName] = childLogger!!
            }
            // current=current.child
            logger = childLogger!!
            // 如果已经到达最后一个元素了，那么return Logger
            if (firstIndex == -1) {
                return logger
            }
        }
    }

    /**
     * 获取name中的第一个"."或者是"$"
     */
    private fun getFirstIndexOf(name: String, fromIndex: Int): Int {
        val i1 = name.indexOf(".", fromIndex)
        val i2 = name.indexOf("$", fromIndex)
        return if (i1 == -1) i2 else i1
    }
}