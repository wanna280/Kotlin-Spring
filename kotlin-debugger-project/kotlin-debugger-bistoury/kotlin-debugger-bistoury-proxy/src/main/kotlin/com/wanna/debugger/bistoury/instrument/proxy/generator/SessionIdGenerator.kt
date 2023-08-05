package com.wanna.debugger.bistoury.instrument.proxy.generator

import com.wanna.framework.context.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * UI-Proxy-Agent之间建立的Session的IdGenerator
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/24
 */
@Component
class SessionIdGenerator : IdGenerator {

    companion object {
        /**
         * 自增ID
         */
        @JvmStatic
        private val AUTO_INCREMENT_ID = AtomicInteger()
    }

    override fun generateId(): String {
        TODO("Not yet implemented")
    }
}