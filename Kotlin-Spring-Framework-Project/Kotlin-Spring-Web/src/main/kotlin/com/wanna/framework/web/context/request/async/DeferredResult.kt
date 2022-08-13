package com.wanna.framework.web.context.request.async

/**
 * DeferredResult，封装一个延时获取的结果
 */
class DeferredResult<T> {

    companion object {
        private val RESULT_NONE: Any = Any()
    }

    // 延时任务的最终结果
    private var result: Any? = RESULT_NONE

    // 处理结果的ResultHandler
    private var resultHandler: DeferredResultHandler? = null

    fun setResult(result: Any?) {
        this.result = result
        this.resultHandler?.handleResult(result)
    }

    fun setErrorResult(ex: Throwable) {
        this.result = ex
        this.resultHandler?.handleResult(ex)
    }

    /**
     * 设置ResultHandler，相当于Future的监听器，对异步任务的最终的结果去进行处理
     *
     * @param resultHandler ResultHandler
     */
    fun setResultHandler(resultHandler: DeferredResultHandler) {
        this.resultHandler = resultHandler

        if (this.result !== RESULT_NONE) {
            resultHandler.handleResult(this.result)
        }
    }

    /**
     * 处理DeferredResult的Handler
     */
    interface DeferredResultHandler {
        fun handleResult(result: Any?)
    }
}