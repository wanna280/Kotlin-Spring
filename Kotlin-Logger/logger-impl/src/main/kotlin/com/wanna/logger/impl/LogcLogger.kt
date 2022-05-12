package com.wanna.logger.impl

import com.wanna.logger.api.Logger
import com.wanna.logger.impl.appender.LoggerAppender
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
        const val ROOT_LOGGER_NAME = "root"

        // Logger的全类名
        val FULL_QUALIFIER_CLASS_NAME = LogcLogger::class.java.name
    }

    // 是否还需要额外的Logger(向parent方向去找)去进行输出，默认情况会一直往parent去找
    private var additive = true

    // loggerContext
    private var abstractLoggerContext: AbstractLoggerContext<out LogcLogger>? = null

    // parent Logger
    private var parent: LogcLogger? = null

    // children Logger，为了保证并发安全，采用COW的ArrayList
    private val children = CopyOnWriteArrayList<LogcLogger>()

    // LoggerName，默认为Logger的全类名，可以设置为getLogger的LoggerName
    private var loggerName = name.ifBlank { this::class.java.name }

    /**
     * Logger自身的Level
     * Level是数字越大等级越高，判断它是否开启了某个级别；
     *
     * <note>要判断是否开启了某个级别，需要判断的是this.level<=targetEvent.level</note>
     */
    private var level = Level.DEBUG

    /**
     * LoggerAppender列表，使用输出流的方式，将日志输出到对应的位置，比如Console/LogFile
     */
    private var appenderList = CopyOnWriteArrayList<LoggerAppender>()

    /**
     * 添加Appender
     *
     * @param appenders 要添加的LoggerAppender列表
     */
    open fun addAppenders(vararg appenders: LoggerAppender) {
        appenderList += appenders
    }

    /**
     * 设置logger的LoggingLevel
     *
     * @param level 要进行设置的level
     */
    open fun setLevel(level: Level) {
        synchronized(this) {
            this.level = level
        }
    }

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
     * 过滤，并且使用Appender去输出日志信息
     *
     * @param loggerQualifierName logger全类名
     * @param level LoggingEvent Level
     * @param msg 输出的消息
     */
    private fun filterAndLog(loggerQualifierName: String, level: Level, msg: Any?) {
        // 让LoggerFilter去决策本次日志信息，是否要进行输出？
        val reply = abstractLoggerContext!!.filterList.getFilterChainDecisionReply(this, level, msg, emptyArray(), null)

        // 如果最终的结果是DENY的话，那么需要拒绝，不进行本次日志的输出，return
        if (reply == FilterReply.DENY) {
            return
        }

        // 构建LoggingEvent并使用Appender其完成Append
        buildLoggingEventAndAppend(loggerQualifierName,level, msg)
    }

    /**
     * 构建LoggingEvent并使用Appender其完成Append
     *
     * @param loggerQualifierName logger全类名
     * @param level 当前的日志级别
     * @param msg 要进行输出的日志消息message
     */
    private fun buildLoggingEventAndAppend(loggerQualifierName: String, level: Level, msg: Any?) {
        // fixed: 应该使用当前的loggerName去进行构建event，而不是等之后再去构建event，不然loggerName会为root
        val event = LoggingEvent(loggerQualifierName, level, msg, this.loggerName)

        // 调用所有的Appender(ConsoleAppender/FileAppender等)
        callAppenders(event)
    }

    open fun callAppenders(event: LoggingEvent) {
        var logger: LogcLogger? = this
        // 如果当前Logger没有Appender去进行日志的输出，但是parent Logger可以去完成日志的输出，那么就交给parent去进行日志的输出
        while (logger != null) {
            if (!logger.hasAppender()) {
                // 如果允许向parent方向去进行寻找的话，向父方向去进行寻找；如果不允许的话，直接break掉跳出循环
                if (logger.additive) {
                    logger = logger.getParent()
                    continue
                }
                break
            }
            // fixed: 这里应该使用event.getLevel().level去进行比较，使用之前的level.level是bug
            if (logger.level.level <= event.getLevel().level) {
                // 遍历所有的Appender，去完成日志消息的Append
                // 这里应该使用logger.getAppenderList，而不是this.appenderList
                logger.getAppenderList().forEach { it.append(event) }
            }
            break
        }
    }

    /**
     * 通过name去获取childLogger，如果获取不到return null
     *
     * @param name loggerName
     * @return 获取到的Logger
     */
    open fun getChildByName(name: String): LogcLogger? {
        for (child in children) {
            if (child.getLoggerName() == name) {
                return child
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
        // 创建一个Logger，并设置parent为this，并将this的children列表当如加入该Logger作为孩子Logger
        val logcLogger = this.abstractLoggerContext!!.newLogger(name)
        logcLogger.setParent(this)
        logcLogger.setLoggerContext(this.abstractLoggerContext!!)
        logcLogger.level = this.level
        children += logcLogger
        return logcLogger
    }

    override fun isDebugEnabled(): Boolean {
        return level.level <= Level.DEBUG.level
    }

    override fun isTraceEnabled(): Boolean {
        return level.level <= Level.TRACE.level
    }

    override fun isInfoEnabled(): Boolean {
        return level.level <= Level.INFO.level
    }

    override fun isWarnEnabled(): Boolean {
        return level.level <= Level.WARN.level
    }

    override fun isErrorEnabled(): Boolean {
        return level.level <= Level.ERROR.level
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
        return ArrayList(appenderList)
    }

    open fun hasAppender(): Boolean = this.appenderList.isNotEmpty()

    override fun setLoggerName(name: String) {
        this.loggerName = name
    }

    open fun setLoggerContext(context: AbstractLoggerContext<out LogcLogger>) {
        this.abstractLoggerContext = context
    }

    override fun toString(): String {
        return "$javaClass[${this.loggerName}]"
    }

    open fun setAdditive(additive: Boolean) {
        this.additive = additive
    }
}