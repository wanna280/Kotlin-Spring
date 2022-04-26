package com.wanna.logger.api

/**
 * 这是顶层的API规范，是LoggerFactory的一层抽象，负责去提供获取和创建Logger的方式
 */
interface ILoggerFactory {

    fun getLogger(name: String): Logger

    fun getLogger(clazz: Class<*>): Logger
}