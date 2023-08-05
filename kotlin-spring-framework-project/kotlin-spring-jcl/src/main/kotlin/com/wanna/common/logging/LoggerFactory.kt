package com.wanna.common.logging

/**
 * LoggerFactory
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 *
 * @see Logger
 */
object LoggerFactory {
    @JvmStatic
    fun getLogger(clazz: Class<*>) = getLogger(clazz.name)

    @JvmStatic
    fun getLogger(name: String): Logger = LogAdapter.createLogger(name)
}