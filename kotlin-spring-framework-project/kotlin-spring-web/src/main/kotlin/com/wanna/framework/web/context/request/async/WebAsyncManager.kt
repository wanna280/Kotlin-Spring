package com.wanna.framework.web.context.request.async

import com.wanna.framework.core.task.AsyncTaskExecutor
import com.wanna.framework.core.task.SimpleAsyncTaskExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable

/**
 * SpringMVC的AsyncManager
 */
class WebAsyncManager {
    companion object {
        /**
         * 异步任务的默认TaskExecutor
         */
        @JvmStatic
        private val DEFAULT_TASK_EXECUTOR: AsyncTaskExecutor = SimpleAsyncTaskExecutor(WebAsyncManager::class.java.name)

        /**
         *  Logger
         */
        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(WebAsyncManager::class.java)

        /**
         * 标识异步任务的结果还没产生
         */
        @JvmStatic
        private val RESULT_NONE = Any()
    }

    /**
     * 异步任务的TaskExecutor
     */
    private var taskExecutor: AsyncTaskExecutor = DEFAULT_TASK_EXECUTOR

    /**
     * 延时任务的结果
     */
    private var deferredResult: DeferredResult<Any?>? = null

    /**
     * 异步任务的结果
     */
    @Volatile
    private var concurrentResult: Any? = RESULT_NONE

    /**
     * 异步的WebRequest
     */
    private var asyncWebRequest: AsyncWebRequest? = null

    /**
     * 并发任务的ResultContext, 保存上下文当中需要传递的参数列表
     */
    @Volatile
    private var concurrentResultContext: Array<Any?>? = null

    /**
     * 设置AsyncWebRequest
     *
     * @param asyncWebRequest AsyncWebRequest
     */
    fun setAsyncWebRequest(asyncWebRequest: AsyncWebRequest) {
        this.asyncWebRequest = asyncWebRequest
    }

    /**
     * 设置处理异步任务的TaskExecutor
     *
     * @param executor 执行异步任务需要使用到的AsyncTaskExecutor
     */
    fun setAsyncTaskExecutor(executor: AsyncTaskExecutor) {
        this.taskExecutor = executor
    }

    /**
     * 判断异步请求的任务是否已经开始执行了
     *
     * @return 如果已经开始了, 那么return true; 否则return false
     */
    fun isConcurrentHandlingStarted(): Boolean = this.asyncWebRequest != null && this.asyncWebRequest!!.isAsyncStarted()

    /**
     * 判断异步请求的任务是否已经结束并产生了结果？
     *
     * @return 如果return true, 说明结果已经产生; 不然就说明结果还没产生, 需要等待下一次再来获取结果
     */
    fun hasConcurrentResult(): Boolean = concurrentResult != RESULT_NONE

    /**
     * 获取异步并发任务的结果
     *
     * @return 异步并发任务的结果
     */
    fun getConcurrentResult(): Any? = this.concurrentResult

    /**
     * 开始去处理一个Callable的任务
     *
     * @param callable 要去进行处理的callable任务
     * @param processingContext 处理Callable当中需要使用的上下文信息, 方便后续去进行获取
     */
    @Suppress("UNCHECKED_CAST")
    fun startCallableProcessing(callable: Callable<*>, vararg processingContext: Any?) {
        this.concurrentResultContext = processingContext as Array<Any?>
        this.startAsyncProcessing(arrayOf((processingContext)))
        this.taskExecutor.submit(Callable {
            // 异步方法去执行Callable, 并将结果去设置到result当中
            // 方便后面去进行处理异步任务的结果, 并完成dispatch
            val result = try {
                callable.call()
            } catch (ex: Exception) {
                ex
            }
            // 设置异步任务的结果, 并完成重新的dispatch
            setConcurrentResultAndDispatch(result)
            result  // return result
        })
    }

    /**
     * 开始去处理一个DeferredResult这样的异步任务的结果
     *
     * @param deferredResult DeferredResult
     * @param processingContext 处理异步任务需要使用到的上下文信息, 方便后续去进行获取
     */
    @Suppress("UNCHECKED_CAST")
    fun startDeferredResultProcessing(deferredResult: DeferredResult<Any?>, vararg processingContext: Any?) {
        this.concurrentResultContext = processingContext as Array<Any?>

        this.deferredResult = deferredResult
        this.startAsyncProcessing(arrayOf(*processingContext))

        // 设置DeferredResult的结果处理器, 当结果到来时, 需要去完成异步任务的设置, 并完成dispatch
        deferredResult.setResultHandler(object : DeferredResult.DeferredResultHandler {
            override fun handleResult(result: Any?) {
                setConcurrentResultAndDispatch(result)
            }
        })
    }

    /**
     * 当异步任务执行完成时, 需要去设置并发(异步)任务的结果, 并去进行派发(交给IO线程池去进行重新doDispatch)
     *
     * @param result 执行异步任务的最终结果
     */
    private fun setConcurrentResultAndDispatch(result: Any?) {
        synchronized(this) {
            if (this.concurrentResult != RESULT_NONE) {
                return
            }
            this.concurrentResult = result
        }

        // 处理结果的Dispatch, 触发AsyncWebRequest的AsyncContext的Action
        this.asyncWebRequest?.dispatch() ?: throw IllegalStateException("AsyncWebRequest不能为空")
    }

    /**
     * 开启异步处理, 这里需要开启AsyncWebRequest的startAsync
     *
     * @param processingContext processingContext
     */
    private fun startAsyncProcessing(processingContext: Array<out Any?>) {
        this.asyncWebRequest?.startAsync()
    }

    /**
     * 清空异步并发任务的结果, 将concurrentResult重设
     */
    fun clearConcurrentResult() {
        synchronized(this@WebAsyncManager) {
            this.concurrentResult = RESULT_NONE
        }
    }
}