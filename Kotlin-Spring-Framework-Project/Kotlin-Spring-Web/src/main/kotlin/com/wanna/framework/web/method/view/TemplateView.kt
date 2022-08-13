package com.wanna.framework.web.method.view

import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.ui.View
import java.io.InputStream

/**
 * 模板View
 */
open class TemplateView : View {

    var viewName: String? = null

    var resourceStream: InputStream? = null

    var resourceUrl: String? = null

    override fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
        val content = resourceStream?.readAllBytes() ?: throw IllegalStateException("无法读取到资源[viewName=$viewName]数据")
        response.getOutputStream().write(content)
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
        response.flush()  // flush
    }
}