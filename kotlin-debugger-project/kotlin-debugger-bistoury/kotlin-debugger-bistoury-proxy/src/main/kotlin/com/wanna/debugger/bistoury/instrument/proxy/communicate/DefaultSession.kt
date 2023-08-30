package com.wanna.debugger.bistoury.instrument.proxy.communicate

import com.wanna.debugger.bistoury.instrument.proxy.communicate.agent.AgentConnection
import com.wanna.debugger.bistoury.instrument.proxy.communicate.ui.UIConnection

/**
 * Session
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/24
 */
open class DefaultSession : Session {



    override fun getSessionId(): String {
        TODO("Not yet implemented")
    }

    override fun getUIConnection(): UIConnection {
        TODO("Not yet implemented")
    }

    override fun getAgentConnection(): AgentConnection {
        TODO("Not yet implemented")
    }
}