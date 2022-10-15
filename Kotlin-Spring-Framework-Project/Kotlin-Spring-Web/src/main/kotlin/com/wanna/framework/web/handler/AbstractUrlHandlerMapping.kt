package com.wanna.framework.web.handler

import com.wanna.framework.web.resource.ResourceHttpRequestHandler
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 抽象的基于Url方式的HandlerMapping
 */
abstract class AbstractUrlHandlerMapping : AbstractHandlerMapping() {
    // HandlerMap
    private val handlerMap = LinkedHashMap<String, Any>()

    init {
        handlerMap["/"] = ResourceHttpRequestHandler()
    }

    override fun getHandlerInternal(request: HttpServerRequest): Any? {
        val lookupPath = initLookupPath(request)

        val handler = lookupHandler(lookupPath, request)

        return handler
    }

    protected open fun initLookupPath(request: HttpServerRequest): String {
        return request.getUrl()
    }

    /**
     * 寻找Handler
     */
    protected open fun lookupHandler(lookupPath: String, request: HttpServerRequest): Any? {
        val handler = getDirectMatch(lookupPath, request)
        if (handler != null) {
            return handler
        }
        return null
    }

    private fun getDirectMatch(lookupPath: String, request: HttpServerRequest): Any? {
        return handlerMap[lookupPath]
    }
}