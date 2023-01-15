package com.wanna.logger.impl.spi

import com.wanna.logger.impl.AbstractLoggerContext
import com.wanna.logger.impl.LogcLogger

/**
 * 为Logger提供SPI的实现机制, 交给使用方, 自己去对LoggerContext去进行初始化操作
 */
interface Configurator {
    fun configure(loggerContext: AbstractLoggerContext<out LogcLogger>)
}