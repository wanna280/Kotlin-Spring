package com.wanna.boot.logging

import com.wanna.framework.core.environment.ConfigurableEnvironment

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class LoggingSystemProperties(environment: ConfigurableEnvironment) {

    open fun apply() {
        apply(null)
    }

    open fun apply(logFile: LogFile?) {

    }

}