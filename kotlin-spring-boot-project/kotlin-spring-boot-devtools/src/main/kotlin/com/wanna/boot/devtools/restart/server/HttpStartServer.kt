package com.wanna.boot.devtools.restart.server

import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.web.http.HttpStatus
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.io.ObjectInputStream

class HttpStartServer(private val server: StartServer) {

    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(HttpStartServer::class.java)
    }

    fun handle(request: HttpServerRequest, response: HttpServerResponse) {
        try {
            val body = request.getInputStream()
            val objectBody = ObjectInputStream(body)
            val classLoaderFiles = objectBody.readObject() as ClassLoaderFiles
            objectBody.close()

            // update and restart
            server.updateAndRestart(classLoaderFiles)
            response.setStatus(HttpStatus.SUCCESS)
        } catch (ex: Exception) {
            logger.warn("无法根据给定的HTTP请求去完成重启", ex)
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}