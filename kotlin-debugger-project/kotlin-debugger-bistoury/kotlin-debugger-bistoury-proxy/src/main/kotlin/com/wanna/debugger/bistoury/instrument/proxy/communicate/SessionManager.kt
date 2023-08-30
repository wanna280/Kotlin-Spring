package com.wanna.debugger.bistoury.instrument.proxy.communicate

import com.wanna.debugger.bistoury.instrument.proxy.communicate.ui.UIConnection

/**
 * Session的Manager
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/22
 */
interface SessionManager {

    /**
     * 根据sessionId去获取Session
     *
     * @param sessionId sessionId
     * @return 获取到的Session(不存在该Session的话, 返回null)
     */
    fun getSession(sessionId: String): Session?

    /**
     * 获取单个UIConnection对应的所有的Session
     *
     * @param uiConnection UIConnection
     * @return 获取到的所有的Session列表
     */
    fun getSessionsByUIConnection(uiConnection: UIConnection): Set<Session>
}