package com.wanna.framework.simple.test.test

import com.wanna.common.logging.LoggerFactory

class Logger {
}

fun main() {
    val logger = LoggerFactory.getLogger(Logger::class.java)
    logger.info("wanna")
}