package com.wanna.debugger.bistoury.instrument.proxy.communicate

import com.wanna.debugger.bistoury.instrument.proxy.communicate.agent.AgentConnection
import com.wanna.debugger.bistoury.instrument.proxy.communicate.ui.UIConnection

/**
 * 描述了从前端(UI)-Proxy-Agent的会话关系
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/22
 */
interface Session {

    /**
     * 获取SessionId
     *
     * @return sessionId
     */
    fun getSessionId(): String

    /**
     * 获取UIConnection(UI-Proxy之间的连接)
     *
     * @return UIConnection
     */
    fun getUIConnection(): UIConnection

    /**
     * 获取AgentConnection(Proxy-Agent之间的连接)
     *
     * @return AgentConnection
     */
    fun getAgentConnection(): AgentConnection
}