package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.impl.filter.LoggerFilterList

/**
 * 这是实现方对于LoggerFactory的具体实现提供模板实现类，实现方必须能遵循的API规范，因为API规范的指定方，会用到ILoggerFactory，去进行getLogger
 *
 * @see ILoggerFactory
 * @param T  Logger类型，子类可以进行自定义自己的LogcLogger类型
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractLoggerContext<T : LogcLogger> : ILoggerFactory {

    // RootLogger，全局默认的Logger
    var root: T = this.initRootLogger()

    // Filter列表
    val filterList: LoggerFilterList = LoggerFilterList()

    // 这是Logger的缓存的默认实现，缓存已经注册过的所有Logger，为了保证线程安全，采用ConcurrentHashMap
    private val loggerCache = this.newLoggerCache()

    init {
        root.setLoggerContext(this)
    }

    /**
     * 创建LoggerCache的方法，模板方法，交给子类去进行扩展
     */
    abstract fun newLoggerCache(): MutableMap<String, T>

    /**
     * 创建一个默认的Logger，name采用自定义的；抽象方法，子类必须实现有名字的创建Logger的方式
     */
    abstract fun newLogger(name: String): T

    /**
     * 初始化RootLogger，子类可以重写这个方法，完成自定义的RootLogger的实现；
     * 也提供了默认的实现，子类也可以不进行实现，使用默认的扩展方法
     */
    open fun initRootLogger(): T {
        return newLogger(LogcLogger.ROOT_LOGGER_NAME)
    }

    /**
     * 创建一个默认的Logger，name采用默认的LogcLogger的类名
     */
    open fun newLogger(): T = newLogger(LogcLogger::class.java.name)

    /**
     * 通过clazz去获取Logger
     *
     * @param clazz 目标类
     * @return 目标类的Logger
     */
    override fun getLogger(clazz: Class<*>): T {
        return getLogger(clazz.name)
    }

    /**
     * 按照name去获取Logger
     *
     * @param name LoggerName
     * @return 获取到的Logger
     */
    override fun getLogger(name: String): T {
        // 如果name给的是ROOT那么直接return，避免因为获取Logger导致加锁
        if (name == root.getLoggerName()) {
            return root
        }
        // 如果给定name不是ROOT的话，那么确实得尝试从loggerCache当中获取了
        var childLogger: T? = loggerCache[name]
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
                childLogger = logger.getChildByName(childName) as T?
                if (childLogger == null) {
                    childLogger = logger.createChildByName(childName) as T
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
     * 获取loggerName当中的第一个"."或者是"$"
     *
     * @param name loggerName
     * @param fromIndex 从哪个位置开始开始进行判断
     * @return "."或者"$"所在位置的index
     */
    private fun getFirstIndexOf(name: String, fromIndex: Int): Int {
        val i1 = name.indexOf(".", fromIndex)
        val i2 = name.indexOf("$", fromIndex)
        return if (i1 == -1) i2 else i1
    }
}