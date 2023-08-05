package com.wanna.logger.api.spi

import com.wanna.logger.api.ILoggerFactory

/**
 * 这是一个API规范下的供SPI的规范, 交给实现方去进行实现, 主要是API设计方得通过它去getLoggerFactory
 */
interface LoggerFactoryBinder {

    fun getLoggerFactory(): ILoggerFactory

    fun getLoggerFactoryClassStr(): String
}