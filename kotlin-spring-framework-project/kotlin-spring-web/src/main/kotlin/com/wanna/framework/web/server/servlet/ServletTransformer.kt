package com.wanna.framework.web.server.servlet

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.Cookie
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerRequestImpl
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.server.HttpServerResponseImpl
import java.lang.StringBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet的Transformer, 将Servlet的Request去转换成为Request, 将Servlet的Response去转换成为Response
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class ServletTransformer {

    /**
     * 将[HttpServletRequest]转换为[HttpServerRequest]
     *
     * @param request servlet request
     * @return request
     */
    open fun transform(request: HttpServletRequest): HttpServerRequest {
        val wrapper = HttpServerRequestWrapper(request)
        // transform headers
        for (headerName in request.headerNames) {
            for (headerValue in request.getHeaders(headerName)) {
                wrapper.addHeader(transformHeaderName(headerName), headerValue)
            }
        }
        // transform paramValues
        request.parameterMap.forEach { (k, v) ->
            for (parameterValue in v) {
                wrapper.addParam(k, parameterValue)
            }
        }
        if (request.cookies !== null) {
            // transform cookies
            val arrayOfCookies = request.cookies.map {
                val cookie = Cookie(it.name, it.value)
                cookie.comment = it.comment
                cookie.httpOnly = it.isHttpOnly
                cookie.domain = it.domain
                cookie.path = it.path
                cookie.secure = it.secure
                cookie.version = it.version
                cookie.maxAge = it.maxAge
                cookie
            }.toTypedArray()
            wrapper.setCookies(*arrayOfCookies)
        }

        wrapper.setInputStream(request.inputStream)
        wrapper.setUrl(request.requestURL.toString())
        wrapper.setUri(request.requestURI)
        wrapper.setMethod(RequestMethod.forName(request.method))

        return wrapper
    }

    /**
     * 把headerName从"content-type"这种方式, 去转换成为驼峰的方式, 例如"Content-Type"
     *
     * @param origin origin headerName
     * @return transformed headerName
     */
    private fun transformHeaderName(origin: String): String {
        val builder = StringBuilder()
        origin.indices.forEach {
            if (it == 0 || origin[it - 1] == '-') {
                builder.append(origin[it].uppercase())
            } else {
                builder.append(origin[it])
            }
        }
        return builder.toString()
    }

    /**
     * 将[HttpServletResponse]去转换成为[HttpServerResponse]
     *
     * @param response servlet response
     * @return response
     */
    open fun transform(response: HttpServletResponse): HttpServerResponse {
        return HttpServerResponseWrapper(response)
    }
}