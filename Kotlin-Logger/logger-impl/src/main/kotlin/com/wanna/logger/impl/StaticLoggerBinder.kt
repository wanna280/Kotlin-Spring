package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.api.spi.LoggerFactoryBinder

/**
 * 这是一个StaticLoggerBinder，供API规范去使用类加载机制去进行加载，从而完成整个Logger上下文的初始化
 */
class StaticLoggerBinder : LoggerFactoryBinder {

    private val defaultLoggerContext = LoggerContext()

    companion object {

        @JvmField
        val SINGLETON = StaticLoggerBinder()

        init {
            // 在通过方法去获取Singleton之前，初始化Singleton
            SINGLETON.init()
        }

        @JvmStatic
        fun getSingleton(): LoggerFactoryBinder {
            return SINGLETON
        }
    }

    /**
     * 完成LoggerContext的初始化工作
     */
    fun init() {
        ContextInitializer(defaultLoggerContext).autoConfig()
    }

    override fun getLoggerFactory(): ILoggerFactory {
        return defaultLoggerContext
    }

    override fun getLoggerFactoryClassStr(): String {
        return this::class.java.name
    }
}