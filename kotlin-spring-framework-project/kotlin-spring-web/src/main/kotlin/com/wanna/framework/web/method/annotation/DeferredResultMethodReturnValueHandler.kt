package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.concurrent.ListenableFuture
import com.wanna.framework.util.concurrent.ListenableFutureCallback
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.context.request.async.DeferredResult
import com.wanna.framework.web.context.request.async.WebAsyncUtils
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.server.HttpServerRequest
import java.lang.UnsupportedOperationException
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage

/**
 * 处理DeferredResult(延时的结果)的方法返回值处理器
 *
 * @see HandlerMethodReturnValueHandler
 */
open class DeferredResultMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    /**
     * 是否支持处理这样的返回值? 只要是DeferredResult/ListenableFuture/CompletableFuture, 那么就支持去进行处理
     *
     * @param parameter 返回值类型封装成为的MethodParameter
     * @return 如果返回值类型是DeferredResult/ListenableFuture/CompletableFuture, 那么return true; 否则return false
     */
    override fun supportsReturnType(parameter: MethodParameter) =
        ClassUtils.isAssignFrom(CompletionStage::class.java, parameter.getParameterType()) ||
                ClassUtils.isAssignFrom(ListenableFuture::class.java, parameter.getParameterType()) ||
                ClassUtils.isAssignFrom(DeferredResult::class.java, parameter.getParameterType())

    /**
     * 处理返回值, 需要处理CompletableFuture/ListenableFuture/DeferredResult等类型的返回值
     *
     * @param returnType 返回值类型封装出来的MethodParameter
     * @param returnValue 返回值对象
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView的容器
     */
    @Suppress("UNCHECKED_CAST")
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue == null) {
            mavContainer.requestHandled = true
            return
        }

        // 将ListenableFuture/CompletableFuture去转换成为DeferredResult
        // DeferredResult主要负责去监听ListenableFuture/CompletableFuture的处理完成的事件
        // 对最终的结果去进行处理, 支持使用监听器的方式去进行完成
        val deferredResult: DeferredResult<Any?> = when (returnValue) {
            is DeferredResult<*> -> returnValue as DeferredResult<Any?>
            is ListenableFuture<*> -> adaptListenableFuture(returnValue as ListenableFuture<Any?>)
            is CompletionStage<*> -> adaptCompletionStage(returnValue as CompletionStage<Any?>)
            else -> throw UnsupportedOperationException("不支持处理这种类型")
        }

        // 交给WebAsyncManager去处理DeferredResult这个异步任务的结果
        WebAsyncUtils.getAsyncManager(webRequest.getNativeRequest(HttpServerRequest::class.java))
            .startDeferredResultProcessing(deferredResult, mavContainer)
    }

    /**
     * 将ListenableFuture转换成为DeferredResult
     *
     * @param future ListenableFuture
     * @return 转换之后的DeferredResult
     */
    private fun adaptListenableFuture(future: ListenableFuture<Any?>): DeferredResult<Any?> {
        val deferredResult = DeferredResult<Any?>()
        future.addCallback(object : ListenableFutureCallback<Any?> {
            override fun onError(ex: Throwable) {
                deferredResult.setErrorResult(ex)
            }

            override fun onSuccess(result: Any?) {
                deferredResult.setResult(result)
            }
        })
        return deferredResult
    }

    /**
     * 将CompletableFuture转换成为DeferredResult
     *
     * @param future 待转换的CompletableFuture
     * @return 转换之后的DeferredResult
     */
    private fun adaptCompletionStage(future: CompletionStage<Any?>): DeferredResult<Any?> {
        val deferredResult = DeferredResult<Any?>()
        future.whenComplete { result, err ->
            if (err != null) {
                if (err is CompletionException && err.cause != null) {
                    deferredResult.setErrorResult(err.cause!!)
                } else {
                    deferredResult.setErrorResult(err)
                }
            } else {
                deferredResult.setResult(result)
            }
        }
        return deferredResult
    }
}