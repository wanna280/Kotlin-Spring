package com.wanna.boot.web.embedded.tomcat

import org.apache.coyote.ProtocolHandler

/**
 * 对Tomcat的ProtocolHandler去进行自定义的自定义化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/12
 */
fun interface TomcatProtocolHandlerCustomizer<T : ProtocolHandler> {

    /**
     * 执行对于Tomcat1的ProtocolHandler的自定义
     *
     * @param protocolHandler ProtocolHandler
     */
    fun customize(protocolHandler: ProtocolHandler)
}