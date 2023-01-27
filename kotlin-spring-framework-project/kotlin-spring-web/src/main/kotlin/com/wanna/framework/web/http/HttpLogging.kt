package com.wanna.framework.web.http

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory

/**
 * HTTP Logging
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
object HttpLogging {

    fun getLogger(primaryLoggerClass: Class<*>): Logger {
        return LoggerFactory.getLogger(primaryLoggerClass)
    }
}