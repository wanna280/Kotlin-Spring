package org.slf4j.impl

import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

/**
 * 这是针对于Slf4j使用的StaticLoggerBinder，去为Slf4j当中的LoggerFactory的获取提供支持
 *
 * @see LoggerFactoryBinder
 */
open class StaticLoggerBinder : LoggerFactoryBinder {

    private val defaultSlf4JBridgeLoggerContext = Slf4JBridgeLoggerContext()

    companion object {
        @JvmField
        val SINGLETON = StaticLoggerBinder()

        init {
            SINGLETON.init()
        }

        /**
         * 这个方法是交给Slf4j去进行回调的，必须有这个方法，不然Slf4j不能完成初始化工作
         */
        @JvmStatic
        fun getSingleton(): StaticLoggerBinder {
            return SINGLETON
        }
    }

    /**
     * 对Logger的上下文完成初始化工作，比如添加FileAppender/LoggerAppender以及LoggingLevel等属性
     *
     * @see Slf4jBridgeContextInitializer
     */
    open fun init() {
        Slf4jBridgeContextInitializer(defaultSlf4JBridgeLoggerContext).autoConfig()
    }

    /**
     * 来自于Slf4j的LoggerFactoryBinder的方法
     *
     * @return Slf4j的ILoggerFactory
     */
    override fun getLoggerFactory(): ILoggerFactory {
        return defaultSlf4JBridgeLoggerContext
    }

    /**
     * 来自于Slf4j的LoggerFactoryBinder的方法
     *
     * @return Slf4j的ILoggerFactory的className
     */
    override fun getLoggerFactoryClassStr(): String {
        return defaultSlf4JBridgeLoggerContext::class.java.name
    }
}