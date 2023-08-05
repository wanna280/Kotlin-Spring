package com.wanna.framework.web

import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory

/**
 * 这是对拦截器链的封装, 内部组合了拦截器链以及处理本次请求的Handler, 对于每个HandlerMapping应该返回的就是一个HandlerExecutionChain
 *
 * @param handler 处理请求的Handler(例如HandlerMethod), 具体是什么类型, 交给HandlerMapping自己去决定, 这里使用的是Any(Object)类型
 * @param interceptors 要使用的拦截器列表
 *
 * @see HandlerInterceptor
 */
open class HandlerExecutionChain(private val handler: Any, interceptors: Collection<HandlerInterceptor>? = null) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(HandlerExecutionChain::class.java)
    }

    // 拦截器链的列表
    private val interceptorList = ArrayList<HandlerInterceptor>()

    // 拦截器链的索引, 控制拦截器链的链式调用
    private var interceptorIndex = -1

    init {
        // 将构造器当中的拦截器列表添加到拦截器列表当中
        if (interceptors != null) {
            interceptorList += interceptors
        }
    }


    /**
     * 应用拦截器的preHandle方法, 一旦其中一个HandlerInterceptor在preHandle当中return false, 那么请求将会结束处理;
     * 直接将之前执行过的拦截器链, 去进行逆方向执行, 回调afterCompletion方法, 去进行请求处理过程当中的收尾工作
     */
    open fun applyPreHandle(request: HttpServerRequest, response: HttpServerResponse): Boolean {
        interceptorList.indices.forEach {
            val interceptor = interceptorList[it]
            // 如果在处理请求之后, 拦截器return false, 说明本次请求不应该交给Handler去进行处理, 直接return
            if (!interceptor.preHandle(request, response, this.handler)) {
                triggerAfterCompletion(request, response, null)
                return false
            }
            interceptorIndex = it  // update interceptorIndex
        }
        return true
    }

    /**
     * 应用拦截器的postHandle方法, 因为到达这里请求已经处理完, 应该逆方向去应用所有的HandlerInterceptor
     *
     * @param request request
     * @param response response
     */
    open fun applyPostHandle(request: HttpServerRequest, response: HttpServerResponse) {
        interceptorList.indices.reversed().forEach {
            val interceptor = interceptorList[it]
            interceptor.postHandle(request, response, this.handler)
        }
    }

    /**
     * 在请求的处理完成之后, 需要执行afterCompletion方法去完成请求收尾工作;
     * 直接从interceptorIndex..0, 逆方向去执行所有的HandlerInterceptor的afterCompletion方法
     *
     * @param request request
     * @param response response
     * @throws Throwable 之前处理请求过程当中的异常
     */
    open fun triggerAfterCompletion(request: HttpServerRequest, response: HttpServerResponse, ex: Throwable?) {
        (0..interceptorIndex).reversed().forEach {
            val interceptor = interceptorList[it]
            try {
                interceptor.afterCompletion(request, response, this.handler, ex)
            } catch (ex2: Throwable) {
                logger.error("HandlerInterceptor处理afterCompletion方法过程当中发生了异常", ex2)
            }
        }
    }

    open fun getHandler(): Any = this.handler

    open fun addInterceptor(interceptor: HandlerInterceptor) {
        this.interceptorList += interceptor
    }

    open fun addInterceptor(index: Int, interceptor: HandlerInterceptor) {
        this.interceptorList.add(index, interceptor)
    }

    open fun addInterceptors(interceptors: Collection<HandlerInterceptor>) {
        this.interceptorList += interceptors
    }

    open fun addInterceptors(vararg interceptors: HandlerInterceptor) {
        this.interceptorList += interceptors
    }

    /**
     * 获取当前的HandlerExecutionChain当中的拦截器列表
     *
     * @return HandlerInterceptor列表
     */
    open fun getInterceptors(): List<HandlerInterceptor> = this.interceptorList

    override fun toString() = "HandlerExecution[handler=${this.handler}, interceptorSize=${this.interceptorList.size}]"
}