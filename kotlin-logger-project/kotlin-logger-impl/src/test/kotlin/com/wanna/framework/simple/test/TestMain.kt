package com.wanna.framework.simple.test

import com.wanna.logger.api.LoggerFactory

class TestMain

fun main() {
    val logger = LoggerFactory.getLogger(TestMain::class.java)
    println(logger)
    logger.info("wanna")
    logger.debug("wanna")
}