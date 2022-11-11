package com.wanna.logger.impl

import com.wanna.logger.api.Logger
import com.wanna.logger.impl.appender.LoggerAppender
import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.event.Level
import com.wanna.logger.impl.event.LoggingEvent
import com.wanna.logger.impl.filter.FilterReply
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 这是一个自定义的Logger，针对于API规范中的Logger提供具体的实现
 * 这个组件必须遵循API规范，因为API规范当中，getLogger获取到的就是这个Logger对象
 *
 * @param name loggerName
 */
open class LogcLogger(name: String) : Logger {
    companion object {
        // 默认的Logger的name
        const val ROOT_LOGGER_NAME = "root"

        // Logger的全类名
        val FULL_QUALIFIER_CLASS_NAME: String = LogcLogger::class.java.name
    }

    // 是否还需要额外的Logger(向parent方向去找)去进行输出，默认情况会一直往parent去找
    // 如果向parent去进行寻找的过程当中遇到了additive=false，则停止继续搜索
    // 但是additive=false时，它的child仍旧可以委托当前的Logger去进行append，除非child被设置为false，才不能继续去委托当前的Logger
    private var additive = true

    // loggerContext
    private var abstractLoggerContext: AbstractLoggerContext<out LogcLogger>? = null

    // parent Logger，只要创建了就会被自动设置，以包名的方式去进行管理
    // 比如com.wanna的parent就是com，com的parent是root；root的parent==null
    private var parent: LogcLogger? = null

    // children Logger，为了保证并发安全，采用COW的ArrayList
    private var children: MutableList<LogcLogger>? = null

    // LoggerName，默认为Logger的全类名，可以设置为getLogger的LoggerName
    private var loggerName = name.ifBlank { this::class.java.name }

    // level，可以为null，只是代表它没有被自定义过；它的effectiveLevelInt会完全随着parent而变更；
    // 当它不为null，代表它被自定义过；它的effectiveLevelInt就不应该随着parent而变更了...
    // 真正有用的level信息为effectiveLevelInt，而不是level本身
    private var level: Level? = null

    // 这个levelInit是当level没有被设置的时候，它会自动继承的它parent的levelInt
    // 当level==null，则当parent的levelInt改变时，child会自动修改levelInt
    private var effectiveLevelInt = 0

    // LoggerAppender列表，使用输出流的方式，将日志输出到对应的位置，比如Console/LogFile
    private var appenderList = CopyOnWriteArrayList<LoggerAppender>()

    override fun info(msg: String) {
        filterAndLog(FULL_QUALIFIER_CLASS_NAME, Level.INFO, msg)
    }

    override fun debug(msg: String) {
        filterAndLog(FULL_QUALIFIER_CLASS_NAME, Level.DEBUG, msg)
    }

    override fun warn(msg: String) {
        filterAndLog(FULL_QUALIFIER_CLASS_NAME, Level.WARN, msg)
    }

    override fun error(msg: String) {
        filterAndLog(FULL_QUALIFIER_CLASS_NAME, Level.ERROR, msg)
    }

    override fun trace(msg: String) {
        filterAndLog(FULL_QUALIFIER_CLASS_NAME, Level.TRACE, msg)
    }

    /**
     * 添加Appender
     *
     * @param appenders 要添加的LoggerAppender列表
     */
    open fun addAppenders(vararg appenders: LoggerAppender) {
        appenderList += appenders
    }

    /**
     * 获取LoggingLevel，可以为空，代表它没有自定义过；不为空时，它的值也就是它的真实level；
     * 如果为返回值为null时，请参考getEffectiveLevelInt/getEffectiveLevel当中获取到的才是真正的level；
     *
     * @return level，可以为空，代表它没有自定义过；不为空时，它的值也就是它的真实level
     * @see getEffectiveLevelInt
     * @see getEffectiveLevel
     */
    open fun getLevel(): Level? = this.level

    /**
     * 获取真实有效的levelInt值
     *
     * @return effectiveLevelInt
     */
    open fun getEffectiveLevelInt(): Int = this.effectiveLevelInt

    /**
     * 获取真实的有效的levelInt值转为的对应等级的Level
     *
     * @return effectiveLevel
     */
    open fun getEffectiveLevel(): Level = Level.parse(this.effectiveLevelInt)


    /**
     * 设置logger的LoggingLevel，并通知所有的childLogger，parentLogger的Level已经发生了改变
     *
     * @param newLevel 要进行设置的level
     * @throws IllegalArgumentException 如果当前是RootLogger，并且newLevel=null，不允许这种情况
     */
    open fun setLevel(newLevel: Level?) {
        synchronized(this) {
            val level = this.level
            // 如果oldLevel==newLevel
            if (level == newLevel) {
                return
            }

            // 如果设置newLevel==null，并且当前就是rootLogger的话，那么肯定是不允许的，抛出不合法参数异常
            if (newLevel == null && isCurrentRootLogger()) {
                throw IllegalArgumentException("root logger的level不能设置为null")
            }

            // 如果不是给root设置newLevel==null的话，那么直接继承父类的level即可
            if (newLevel == null) {
                this.level = getParent()!!.level
                this.effectiveLevelInt = getParent()!!.effectiveLevelInt

                // 如果newLevel不为空的话，那么替换掉当前的logger的level
            } else {
                this.level = newLevel
                this.effectiveLevelInt = newLevel.level
            }
            val children = this.children
            // 如果有childLogger的话，那么通知childLogger，parentLevel已经发生了改变
            if (children != null) {
                val size = children.size
                for (index in 0 until size) {
                    children[index].handleParentLevelChange(this.effectiveLevelInt)
                }
            }
        }
    }

    /**
     * 处理parent的level发生改变时，child应该做出的操作；
     * 如果当前的logger的level没有被自定义过，那么将会接收parent的level变更；
     * 如果当前的logger的level被自定义过，那么pass掉，不必处理(因为已经自定义过了！)；
     *
     * @param parentEffectiveLevelInt parentEffectiveLevelInt
     */
    open fun handleParentLevelChange(parentEffectiveLevelInt: Int) {
        synchronized(this) {
            // 如果我的level没有被设置过，那么我跟着parent一起变动；如果我的level被设置过了，那么我就不动了，因为我被自定义过了，我不能听parent的呀！
            if (this.level == null) {
                this.effectiveLevelInt = parentEffectiveLevelInt  // 继承
                val children = this.children
                if (children != null) {
                    val size = children.size
                    for (index in 0 until size) {
                        children[index].handleParentLevelChange(this.effectiveLevelInt)
                    }
                }
            }
        }
    }

    /**
     * (如果有Appender的话)循环调用所有的Appender去完成append
     *
     * @param event LoggingEvent
     */
    open fun appendLoopOnAppenders(event: ILoggingEvent): Int {
        if (this.hasAppender()) {
            getAppenderList().forEach { it.append(event) }
            return 1
        }
        return 0
    }

    /**
     * 使用Filter去决策是否本次请求需要去进行输出，并且如果符合要求的话，使用合适的Appender去输出日志信息
     *
     * @param loggerQualifierName logger全类名
     * @param level LoggingEvent Level
     * @param msg 输出的消息
     */
    private fun filterAndLog(loggerQualifierName: String, level: Level, msg: Any?) {
        // 让LoggerFilter去决策本次日志信息，是否应该要进行输出？
        val reply = abstractLoggerContext!!.getFilterChainDecisionReply(this, level, msg, emptyArray(), null)

        // 如果最终的结果是DENY的话，那么需要拒绝，不进行本次日志的输出，return
        if (reply == FilterReply.DENY) {
            return
            // 如果最终的决策结果是NEUTRAL的话，需要比较正确的LoggingLevel是否和给定的level是否合法
        } else if (reply == FilterReply.NEUTRAL) {
            // 判断日志的级别是否符合要求，如果不符合要求，直接return
            if (this.effectiveLevelInt > level.level) {
                return
            }
        }
        // 如果reply==ACCEPT，或者reply==NEUTRAL并且日志级别符合level的要求的话
        // 那么需要去构建LoggingEvent并使用所有的符合要求的Appender去完成append
        buildLoggingEventAndAppend(loggerQualifierName, level, msg)
    }

    /**
     * 构建LoggingEvent并使用Appender其完成Append
     *
     * @param loggerQualifierName logger全类名
     * @param level 当前的日志级别
     * @param msg 要进行输出的日志消息message
     */
    private fun buildLoggingEventAndAppend(loggerQualifierName: String, level: Level, msg: Any?) {
        // fixed: 应该使用当前的loggerName去进行构建event，而不是等之后再去构建event，不然loggerName会变为它的parent
        val event = LoggingEvent(loggerQualifierName, level, msg, this)

        // 调用所有的Appender(ConsoleAppender/FileAppender等)
        callAppenders(event)
    }

    /**
     * 调用所有的Appender，如果允许委托的的话(additive=true)，会向parent方向去进行搜索
     *
     * @param event LoggingEvent
     */
    open fun callAppenders(event: LoggingEvent) {
        var logger: LogcLogger? = this
        // 如果当前Logger没有Appender去进行日志的输出，但是parent Logger可以去完成日志的输出，那么就交给parent去进行日志的输出
        // note:这里会一直向parent进行委托，也就是如果child有Appender，parent也有Appender，那么都会去进行输出
        while (logger != null) {
            // 交给Logger去循环调用Appender去完成日志的输出
            logger.appendLoopOnAppenders(event)
            // 如果到了当前的Logger位置，已经不允许再向parent去进行委托了，那么直接break掉
            if (!logger.additive) {
                return
            }
            // 如果允许向parent方向去进行委托寻找的话，向父方向去进行寻找；
            logger = logger.getParent()
        }
    }

    /**
     * 通过childLoggerName去获取childLogger，如果获取不到return null
     *
     * @param childName childLoggerName
     * @return 获取到的Logger(有可能获取不到return null)
     */
    open fun getChildByName(childName: String): LogcLogger? {
        val children = this.children
        if (children != null) {
            val size = children.size
            for (index in 0 until size) {
                val child = children[index]
                if (child.getLoggerName() == childName) {
                    return child
                }
            }
        }
        return null
    }

    /**
     * 根据name去创建childLogger，并加入到当前的Logger的children列表当中去
     *
     * @param name loggerName
     * @return 创建好的Logger
     */
    open fun createChildByName(name: String): LogcLogger {
        // 如果children列表还没完成初始化，那么首先去进行初始化操作
        if (this.children == null) {
            this.children = CopyOnWriteArrayList()
        }
        // 创建一个Logger，并设置parent为this，并将this的children列表当如加入该Logger作为孩子Logger
        val logcLogger = this.abstractLoggerContext!!.newLogger(name)
        logcLogger.setParent(this)
        logcLogger.setLoggerContext(this.abstractLoggerContext!!)
        logcLogger.effectiveLevelInt = this.effectiveLevelInt  // 继承level
        this.children!! += logcLogger
        return logcLogger
    }

    override fun isDebugEnabled(): Boolean {
        return effectiveLevelInt <= Level.DEBUG.level
    }

    override fun isTraceEnabled(): Boolean {
        return effectiveLevelInt <= Level.TRACE.level
    }

    override fun isInfoEnabled(): Boolean {
        return effectiveLevelInt <= Level.INFO.level
    }

    override fun isWarnEnabled(): Boolean {
        return effectiveLevelInt <= Level.WARN.level
    }

    override fun isErrorEnabled(): Boolean {
        return effectiveLevelInt <= Level.ERROR.level
    }

    override fun getLoggerName(): String {
        return loggerName
    }

    override fun getParent(): LogcLogger? {
        return parent
    }

    override fun setParent(logger: Logger) {
        this.parent = logger as LogcLogger
    }

    open fun getAppenderList(): List<LoggerAppender> {
        return this.appenderList
    }

    /**
     * 判断当前Logger当中是否有Appender
     *
     * @return 如果有return true；不然return false
     */
    open fun hasAppender(): Boolean = this.appenderList.isNotEmpty()

    open fun getLoggerContext(): AbstractLoggerContext<out LogcLogger>? {
        return this.abstractLoggerContext
    }

    override fun setLoggerName(name: String) {
        this.loggerName = name
    }

    open fun setLoggerContext(context: AbstractLoggerContext<out LogcLogger>) {
        this.abstractLoggerContext = context
    }

    override fun toString(): String = "LogcLogger[${this.loggerName}]"

    /**
     * 是否还需要额外的Logger(向parent方向去找)去进行输出，设置为true会一直往parent去找；false则表示不会
     *
     * @param additive 你要设置的additive(true/false)
     */
    open fun setAdditive(additive: Boolean) {
        this.additive = additive
    }

    /**
     * 判断当前logger是否是rootLogger
     *
     * @return 如果当前logger的root，return true；不然return false
     */
    private fun isCurrentRootLogger(): Boolean = getParent() != null
}