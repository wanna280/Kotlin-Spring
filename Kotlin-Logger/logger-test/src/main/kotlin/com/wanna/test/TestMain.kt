package com.wanna.test

import org.slf4j.LoggerFactory


class TestMain

fun main() {
    Class.forName("org.slf4j.impl.StaticLoggerBinder")

    val logger = LoggerFactory.getLogger(TestMain::class.java)
    println(logger.isTraceEnabled())  // false
    println(logger.isDebugEnabled())  // true
    logger.info("wanna")
    logger.debug("wanna2")
    logger.trace("wanna3")
}