package com.wanna.boot.autoconfigure.web.servlet

import com.wanna.boot.context.properties.ConfigurationProperties

/**
 * Servlet的WebServer的相关配置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
@ConfigurationProperties("server")
class ServletWebServerProperties {

    /**
     * port
     */
    var port: Int = 9966
}