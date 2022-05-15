package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.handler.HandlerExceptionResolver
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

open class ExceptionHandlerExceptionResolver : HandlerExceptionResolver {

    private var messageConverters: List<HttpMessageConverter<*>>? = null

    private var argumentResolvers: List<HandlerMethodArgumentResolver>? = null

    private var returnValueHandlers: List<HandlerMethodReturnValueHandler>? = null

    private var contentNegotiationManager: ContentNegotiationManager = ContentNegotiationManager()

    override fun resolveException(
        request: HttpServerRequest,
        response: HttpServerResponse,
        handler: Any?,
        ex: Throwable
    ): ModelAndView? {
        return null
    }

    open fun getContentNegotiationManager(): ContentNegotiationManager {
        return this.contentNegotiationManager
    }

    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }

    open fun setHttpMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        this.messageConverters = converters
    }

    open fun setHandlerMethodArgumentResolvers(resolvers: List<HandlerMethodArgumentResolver>) {
        this.argumentResolvers = resolvers
    }

    open fun getHttpMessageConverters(): List<HttpMessageConverter<*>> {
        if (this.messageConverters == null) {
            throw IllegalStateException("请先初始化RequestMappingHandlerAdapter的MessageConverter列表")
        }
        return this.messageConverters!!
    }

    open fun setHandlerMethodReturnValueHandlers(handlers: List<HandlerMethodReturnValueHandler>) {
        this.returnValueHandlers = handlers
    }
}