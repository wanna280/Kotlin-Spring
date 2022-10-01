package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.context.request.async.WebAsyncUtils
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.server.HttpServerRequest
import java.util.concurrent.Callable

/**
 * 对于Callable的返回值类型的处理器
 *
 * @see Callable
 * @see HandlerMethodReturnValueHandler
 */
open class CallableMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    /**
     * 是否支持处理这样的类型的返回值？只支持去处理Callable类型的返回值类型
     *
     * @param parameter 返回值类型封装成为的MethodParameter
     * @return 如果返回值类型是Callable，那么return true；否则return false
     */
    override fun supportsReturnType(parameter: MethodParameter): Boolean =
        ClassUtils.isAssignFrom(Callable::class.java, parameter.getParameterType())

    /**
     * 处理Callable类型的返回值类型
     *
     * @param returnType 返回值类型
     * @param returnValue 执行目标方法的返回值
     * @param webRequest NativeWebRequest
     * @param mavContainer ModelAndViewContainer
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue == null) {
            mavContainer.requestHandled = true  // this request has been handled
            return
        }

        // 交给AsyncManager去对Callable去进行处理，去进行异步任务的执行
        WebAsyncUtils.getAsyncManager(webRequest.getNativeRequest(HttpServerRequest::class.java))
            .startCallableProcessing(returnValue as Callable<*>, mavContainer)
    }
}