package com.wanna.framework.web.context.request.async

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 为Web的Async功能提供支持的工具类
 *
 * @see WebAsyncManager
 */
object WebAsyncUtils {

    /**
     * WebAsyncManager的属性，可以根据这个属性去request当中去寻找到WebAsyncManager
     *
     * @see HttpServerRequest.getAttribute
     */
    @JvmStatic
    val WEB_ASYNC_MANAGER_ATTRIBUTE = WebAsyncManager::class.java.name + ".WEB_ASYNC_MANAGER"

    /**
     * 根据request去获取到WebAsyncManager，提供异步API的支持
     *
     * * 1.如果之前已经存在，那么从请求属性当中获取到已经存在有的；
     * * 2.如果之前不存在，那么创建一个新的WebAsyncManager放到请求属性当中
     *
     * @param request request
     * @return 当前请求对应的WebAsyncManager
     */
    @JvmStatic
    fun getAsyncManager(request: HttpServerRequest): WebAsyncManager {
        var asyncManager: WebAsyncManager? = null
        val webAsyncManagerAttr = request.getAttribute(WEB_ASYNC_MANAGER_ATTRIBUTE)
        if (webAsyncManagerAttr is WebAsyncManager) {
            asyncManager = webAsyncManagerAttr
        }
        if (asyncManager == null) {
            asyncManager = WebAsyncManager()  // create empty WebAsyncManager
            request.setAttribute(WEB_ASYNC_MANAGER_ATTRIBUTE, asyncManager)
        }
        return asyncManager
    }

    /**
     * 根据request和response去创建一个AsyncWebRequest
     *
     * @param request request
     * @param response response
     * @return 构建出来的AsyncWebRequest
     */
    @JvmStatic
    fun createAsyncWebRequest(request: HttpServerRequest, response: HttpServerResponse): AsyncWebRequest =
        StandardServerAsyncWebRequest(request, response)
}