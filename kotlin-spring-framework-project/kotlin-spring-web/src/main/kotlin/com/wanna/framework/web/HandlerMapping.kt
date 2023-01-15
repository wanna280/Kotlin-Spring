package com.wanna.framework.web

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个Handler的请求映射中心, 负责注册Controller路径与对应的Handler方法之间的映射关系;
 */
interface HandlerMapping {
    companion object {
        /**
         * 请求当中的uri的模板变量属性的属性名
         */
        @JvmStatic
        val URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping::class.java.name + ".uriTemplateVariables"

        /**
         * 产出的MediaType列表的属性名
         */
        @JvmStatic
        val PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping::class.java.name + ".producibleMediaTypes"

        /**
         * 在HandlerMapping当中去进行匹配时, 最终寻找到的处理请求的路径
         */
        @JvmStatic
        val PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping::class.java.name + ".pathWithinHandlerMapping"
    }


    /**
     * 根据request去获取到HandlerExecutionChain去执行请求的处理; 在HandlerExecutionChain当中包含了处理本次请求的Handler,
     * 以及拦截请求的各个HandlerInterceptor链条
     *
     * @see HandlerExecutionChain
     * @return 如果当前HandlerMapping能处理本次请求, 那么return HandlerExecutionChain; 如果不能处理, return null
     */
    @Nullable
    fun getHandler(request: HttpServerRequest): HandlerExecutionChain?
}