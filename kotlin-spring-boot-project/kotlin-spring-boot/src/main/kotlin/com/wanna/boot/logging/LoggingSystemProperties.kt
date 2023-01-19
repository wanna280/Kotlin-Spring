package com.wanna.boot.logging

import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 * 对于[LoggingSystem]的相关配置信息, 主要从[ConfigurableEnvironment]当中去进行提取
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class LoggingSystemProperties(private val environment: ConfigurableEnvironment) {
    companion object {
        /**
         * 包含了进程的PID的系统属性Key
         */
        const val PID_KEY = "PID"

        /**
         * 包含了"exception conversion word"的系统属性Key
         */
        const val EXCEPTION_CONVERSION_WORD = "LOG_EXCEPTION_CONVERSION_WORD"

        /**
         * 包含了日志文件的系统属性Key
         */
        const val LOG_FILE = "LOG_FILE"

        /**
         * 包含了日志文件的路径的系统属性Key
         */
        const val LOG_PATH = "LOG_PATH"
    }


    open fun apply() {
        apply(null)
    }

    open fun apply(logFile: LogFile?) {

    }

}