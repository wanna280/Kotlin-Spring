package com.wanna.logger.impl

import com.wanna.logger.api.ILoggerFactory
import com.wanna.logger.api.Logger
import com.wanna.logger.api.spi.LoggerFactoryBinder

class StaticLoggerBinder : LoggerFactoryBinder {


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

    fun init() {

    }

    override fun getLoggerFactory(): ILoggerFactory {
        return object : ILoggerFactory {
            override fun getLogger(name: String): Logger {
                TODO("Not yet implemented")
            }

            override fun getLogger(clazz: Class<*>): Logger {
                TODO("Not yet implemented")
            }

            override fun newLogger(name: String): Logger {
                TODO("Not yet implemented")
            }
        }
    }

    override fun getLoggerFactoryClassStr(): String {
        return this::class.java.name
    }
}