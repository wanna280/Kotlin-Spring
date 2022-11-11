package com.wanna.framework.web.handler

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 抽象的基于Url方式的HandlerMapping
 */
abstract class AbstractUrlHandlerMapping : AbstractHandlerMapping() {
    /**
     * HandlerMap
     */
    private val handlerMap = LinkedHashMap<String, Any>()

    override fun getHandlerInternal(request: HttpServerRequest): Any? {
        val lookupPath = initLookupPath(request)

        val handler = lookupHandler(lookupPath, request)

        return handler
    }

    protected open fun initLookupPath(request: HttpServerRequest): String {
        return request.getUrl()
    }

    /**
     * 注册一个Handler到HandlerMap当中来
     *
     * @param url url
     * @param handler handler
     */
    protected open fun registerHandler(url: String, handler: Any) {
        this.handlerMap[url] = handler
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