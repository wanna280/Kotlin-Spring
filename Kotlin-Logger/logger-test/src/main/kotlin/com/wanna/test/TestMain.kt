package com.wanna.test

import com.wanna.logger.api.LoggerFactory

class TestMain

fun main() {
    val logger = LoggerFactory.getLogger(TestMain::class.java)
    println(logger.isTraceEnabled())  // false
    println(logger.isDebugEnabled())  // true
    logger.info("wanna")
    logger.debug("wanna2")
    logger.trace("wanna3")
}