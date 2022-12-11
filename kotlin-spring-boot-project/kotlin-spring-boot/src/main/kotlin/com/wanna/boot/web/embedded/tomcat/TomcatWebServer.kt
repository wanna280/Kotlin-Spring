package com.wanna.boot.web.embedded.tomcat

import com.wanna.boot.web.server.WebServer
import org.apache.catalina.startup.Tomcat
import org.slf4j.LoggerFactory

/**
 * Tomcat WebServer
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class TomcatWebServer(private val tomcat: Tomcat) : WebServer {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(TomcatWebServer::class.java)
    }

    override fun start() {
        tomcat.start()
        logger.info("Tomcat Web Server在[{}]端口启动", getPort())
    }

    override fun stop() {
        tomcat.stop()
    }

    override fun getPort(): Int {
        return tomcat.connector.port
    }

    override fun setPort(port: Int) {
        tomcat.connector.port = port
    }
}