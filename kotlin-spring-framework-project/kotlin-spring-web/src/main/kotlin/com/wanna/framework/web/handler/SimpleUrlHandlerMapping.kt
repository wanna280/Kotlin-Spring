package com.wanna.framework.web.handler

/**
 * 负责处理直接的url的请求, 常见的比如请求资源文件的请求
 */
open class SimpleUrlHandlerMapping : AbstractUrlHandlerMapping() {

    /**
     * UrlMap
     */
    private var urlMap: Map<String, Any> = LinkedHashMap()

    open fun setUrlMap(urlMap: Map<String, Any>) {
        this.urlMap = urlMap
    }

    open fun getUrlMap(): Map<String, Any> = this.urlMap

    /**
     * 在初始化ApplicationContext的同时, 也去注册一下处理URL映射的Handler
     *
     * @see urlMap
     * @see handlerMap
     */
    override fun initApplicationContext() {
        super.initApplicationContext()

        // 将urlMap当中的Handler转移到父类的handlerMap当中去
        registerHandlers(urlMap)
    }

    /**
     * 将urlMap注册到父类的HandlerMap当中去
     *
     * @param urlMap urlMap
     */
    protected open fun registerHandlers(urlMap: Map<String, Any>) {
        urlMap.forEach { (url, handler) ->
            var urlToUse = url
            if (!urlToUse.startsWith("/")) {
                urlToUse = "/$urlToUse"
            }
            registerHandler(urlToUse, handler)
        }
    }
}