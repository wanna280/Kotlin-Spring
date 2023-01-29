package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.filter.FilterReply
import com.wanna.logger.impl.filter.LoggerFilter
import com.wanna.logger.impl.filter.LoggerFilterList
import java.util.concurrent.ConcurrentHashMap

/**
 * 这是实现方对于LoggerFactory的具体实现提供模板实现类, 实现方必须能遵循的API规范, 因为API规范的指定方, 会用到ILoggerFactory, 去进行getLogger
 *
 * @see ILoggerFactory
 * @see LogcLogger
 * @param T  Logger类型, 子类可以进行自定义自己的LogcLogger类型, 必须为LogcLogger的子类
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractLoggerContext<T : LogcLogger> : ILoggerFactory {

    // RootLogger, 全局默认的Logger
    var root: T = this.initRootLogger()

    // LoggerFilter列表, 它能联合列表当中的所有的LoggerFilter一起来决策当前日志是否需要去进行输出
    private val filterList: LoggerFilterList = LoggerFilterList()

    // 这是Logger的缓存的默认实现, 缓存已经注册过的所有Logger, 为了保证线程安全, 采用ConcurrentHashMap
    private val loggerCache = this.newLoggerCache()

    init {
        root.setLoggerContext(this)
    }

    /**
     * 创建LoggerCache的方法, 默认支持使用ConcurrentHashMap的方式; 同时也可以作为模板方法, 交给子类去进行扩展
     *
     * @param T Logger类型
     * @return 要采用的LoggerCache
     */
    open fun newLoggerCache(): MutableMap<String, T> = ConcurrentHashMap<String, T>()

    /**
     * 创建一个默认的Logger, name采用自定义的; 抽象方法, 子类必须实现有名字的创建Logger的方式;
     * 全局创建Logger, 都会使用这个方法去进行创建Logger, 子类完成可以在创建Logger时, 去进行各种的自定义操作;
     *
     * @param name loggerName
     * @return 创建好的Logger
     */
    abstract fun newLogger(name: String): T

    /**
     * 初始化RootLogger, 子类可以重写这个方法, 完成自定义的RootLogger的实现;
     * 也提供了默认的实现, 子类也可以不进行实现, 使用默认的扩展方法
     *
     * @return RootLogger
     */
    open fun initRootLogger(): T {
        return newLogger(LogcLogger.ROOT_LOGGER_NAME)
    }

    /**
     * 添加LoggerFilter
     *
     * @param filter 你想要添加的LoggerFilter
     */
    open fun addLoggerFilter(filter: LoggerFilter) {
        this.filterList += filter
    }

    /**
     * 在指定的index处添加LoggerFilter
     *
     * @param index index
     * @param filter 你想要添加的LoggerFilter
     * @throws IllegalArgumentException 如果index越界
     */
    open fun addLoggerFilterAt(index: Int, filter: LoggerFilter) {
        if (index < 0 || index >= this.filterList.size) {
            throw IllegalArgumentException("给定的index不合法")
        }
        this.filterList.add(index, filter)
    }

    /**
     * 移除LoggerFilter
     *
     * @param filter 你想要移除的LoggerFilter
     */
    open fun removeLoggerFilter(filter: LoggerFilter) {
        this.filterList += filter
    }

    /**
     * 在指定的index处移除该位置的LoggerFilter
     *
     * @param index index
     * @throws IllegalArgumentException 如果index越界的话
     */
    open fun removeLoggerFilterAt(index: Int) {
        if (index < 0 || index >= this.filterList.size) {
            throw IllegalArgumentException("给定的index不合法")
        }
        this.filterList.removeAt(index)
    }

    /**
     * 按照loggerName去获取Logger, 比如给定的loggerName为com.wanna.test.App;
     * 那么它会依次去检测com、com.wanna、com.wanna.framework.simple.test、以及com.wanna.test.App的Logger;
     * 如果对应的name的Logger不存在的话, 那么会先创建对应的Logger, 并加入到LoggerCache当中, 下次就能直接从LoggerCache当中去获取到Logger了
     *
     * @param name LoggerName(一般为全类名)
     * @return 获取到的Logger
     */
    override fun getLogger(name: String): T {
        // 如果name给的是ROOT(第二个参数设置不分大小写)那么直接return, 避免因为获取Logger导致加锁
        if (name.contentEquals(root.getLoggerName(), true)) {
            return root
        }
        // 如果给定name不是ROOT的话, 那么确实得尝试从loggerCache当中获取了
        var childLogger: T? = loggerCache[name]
        if (childLogger != null) {
            return childLogger
        }
        var logger = root
        var index = 0

        // childName记录的是子包名, 比如com.wanna.test.TestMain, 第一次获取的childName=com, 第二次获取的childName=com.wanna
        // 第三次获取的childName=com.wanna.framework.simple.test, 以此类推...
        // 如果childName并未在Logger的children当中的话, 为childName创建Logger并加入到缓存当中
        // 如果childName已经在Logger的children当中的话, 很可能就是之前已经完成过Logger的注册的情况了
        while (true) {
            // 获取name从index位置开始第一个的"."(或者"$")的位置索引firstIndex
            val firstIndex = getFirstIndexOf(name, index)
            // 使用substring的方式去获取子包名, 为所有子包创建(或者获取)Logger
            val childName = if (firstIndex == -1) name else name.substring(0, firstIndex)

            // startIndex=firstIndex+1, 跳过当前位置的".", 为了下次startIndex能直接从".之后"去进行统计
            index = firstIndex + 1

            // 对Logger去进行加锁, 因为可能需要操作它的children列表
            synchronized(logger) {
                // 获取childLogger, 如果必要的话, 先去创建一个, 并加入到缓存当中
                childLogger = logger.getChildByName(childName) as T?
                if (childLogger == null) {
                    childLogger = logger.createChildByName(childName) as T
                    loggerCache[childName] = childLogger!!
                }
            }
            // current=current.child
            logger = childLogger!!
            // 如果已经到达最后一个元素了, 那么return Logger
            if (firstIndex == -1) {
                return logger
            }
        }
    }

    /**
     * 使用Filter去决策是否本次请求需要去进行输出, 并且如果符合要求的话, 需要使用合适的Appender去输出日志信息
     *
     * @param logger Logger
     * @param params 参数列表
     * @param throwable 异常信息
     * @param level LoggingEvent Level
     * @param msg 输出的消息
     * @return 过滤器链决策的结果, DENY/ACCEPT/NEUTRAL
     */
    open fun getFilterChainDecisionReply(
        logger: LogcLogger, level: Level, msg: Any?, params: Array<Any?>, throwable: Throwable?
    ): FilterReply {
        return filterList.getFilterChainDecisionReply(logger, level, msg, emptyArray(), null)
    }

    /**
     * 获取loggerName当中的第一个"."或者是"$"所在的位置index
     *
     * @param name loggerName
     * @param fromIndex 从哪个位置开始开始进行判断
     * @return loggerName当中的"."或者"$"所在位置的index, 如果都不存在, return -1
     */
    private fun getFirstIndexOf(name: String, fromIndex: Int): Int {
        val i1 = name.indexOf(".", fromIndex)
        val i2 = name.indexOf("$", fromIndex)
        return if (i1 == -1) i2 else i1
    }
}