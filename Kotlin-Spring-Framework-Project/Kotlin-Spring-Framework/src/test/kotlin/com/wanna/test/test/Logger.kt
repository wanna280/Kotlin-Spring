package com.wanna.test.test

import org.slf4j.LoggerFactory

class Logger {
}

fun main() {
    val logger = LoggerFactory.getLogger(Logger::class.java)
    logger.info("wanna")
}