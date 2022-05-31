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

    override fun render(model: Map<String, *>?, request: HttpServerRequest, response: HttpServerResponse) {
        val content = resourceStream!!.readAllBytes()
        response.getOutputStream().write(content)
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
    }
}