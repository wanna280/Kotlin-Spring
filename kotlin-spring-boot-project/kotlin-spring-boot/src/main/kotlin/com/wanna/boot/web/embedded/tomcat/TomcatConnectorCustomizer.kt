package com.wanna.boot.web.embedded.tomcat

import org.apache.catalina.connector.Connector

/**
 * Tomcat的Connector的初始化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
interface TomcatConnectorCustomizer {

    /**
     * 执行对于Tomcat的Connector的初始化
     *
     * @param connector Connector
     */
    fun customize(connector: Connector)
}