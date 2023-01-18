package com.wanna.boot.logging

import org.slf4j.Logger
import java.util.function.Supplier

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class DeferredLogs : DeferredLogFactory {

    override fun getLog(destination: Supplier<Logger>): Logger {
        TODO("Not yet implemented")
    }
}