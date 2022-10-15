package com.wanna.boot.diagnostics

import com.wanna.boot.SpringBootExceptionReporter
import com.wanna.framework.context.ConfigurableApplicationContext
import org.slf4j.LoggerFactory

/**
 * SpringBoot的Failure分析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @see SpringBootExceptionReporter
 */
class FailureAnalyzers(private val context: ConfigurableApplicationContext) : SpringBootExceptionReporter {
    companion object {
        /**
         * Logger
         */
        private val logger = LoggerFactory.getLogger(FailureAnalyzers::class.java)
    }

    override fun report(failure: Throwable): Boolean {
        return true
    }
}