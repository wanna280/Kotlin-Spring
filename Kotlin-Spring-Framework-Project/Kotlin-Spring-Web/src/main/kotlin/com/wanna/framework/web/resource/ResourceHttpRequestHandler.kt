package com.wanna.framework.web.resource

import com.wanna.framework.core.io.Resource
import com.wanna.framework.web.HttpRequestHandler
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import org.slf4j.LoggerFactory

/**
 * Spring的资源的HttpRequestHandler，负责返回资源(html/css/js/jpg/png等)给客户端
 *
 * @see HttpRequestHandler
 */
open class ResourceHttpRequestHandler : HttpRequestHandler {

    companion object {
        /**
         * Logger
         */
        private val logger = LoggerFactory.getLogger(ResourceHttpRequestHandler::class.java)
    }

    /**
     * 资源路径
     */
    private var locations: List<Resource>? = null

    open fun setLocations(locations: List<Resource>) {
        this.locations = locations
    }

    override fun handleRequest(request: HttpServerRequest, response: HttpServerResponse) {
        locations?.forEach {
            try {
                val resource = it.createRelative(request.getUrl())
                val inputStream = resource.getInputStream()
                inputStream.use { ips ->
                    response.getOutputStream().write(ips.readAllBytes())
                    response.flush()
                }
            } catch (ex: Exception) {
                logger.error("读取资源失败", ex)
            }
        }
    }
}