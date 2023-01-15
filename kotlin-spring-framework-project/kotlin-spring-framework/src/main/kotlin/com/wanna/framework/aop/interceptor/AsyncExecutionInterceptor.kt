package com.wanna.framework.aop.interceptor

import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.core.task.AsyncTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.function.Supplier

/**
 * 提供异步执行的Execution的MethodInterceptor, 负责拦截目标@Async方法的执行
 *
 * @param executor 想要使用的默认线程池, 在@Async当中配置的和beanFactory当中都找不到默认线程池时去进行使用
 * @param exceptionHandler ExceptionHandler
 */
open class AsyncExecutionInterceptor(
    executor: Supplier<Executor>?, exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?
) : MethodInterceptor, AsyncExecutionAspectSupport(executor, exceptionHandler) {

    /**
     * 执行目标异步方法的时候, 拦截下来目标方法, 交给线程池去进行执行
     *
     * @param invocation MethodInvocation
     * @return 异步方法的返回值, CompletableFuture/Future/Null
     */
    override fun invoke(invocation: MethodInvocation): Any? {
        val returnType = invocation.getMethod().returnType
        // 尝试推断异步线程池, 1.尝试从方法/类上的@Async注解当中去进行寻找, 2.尝试从beanFactory当中获取, 3.使用指定的默认Executor
        val executor: AsyncTaskExecutor =
            determineAsyncExecutor(invocation.getMethod()) ?: throw IllegalStateException("无法找到合适的Executor去执行异步任务")

        // 根据方法的MethodInvocation去构建一个Callable的Task
        val task = Callable {
            try {
                val returnValue = invocation.proceed()
                if (returnValue is Future<*>) {
                    return@Callable returnValue.get()
                }
            } catch (ex: Exception) {  // handleException
                handleException(ex, invocation.getMethod(), *invocation.getArguments()!!)
            }
            return@Callable null
        }
        // 提交任务, 并return...支持去返回CompletableFuture/Future/Void
        return doSubmit(task, executor, returnType)
    }

    /**
     * 默认实现获取Executor的Qualifier的方式为没有(使用默认的)
     *
     * @param method 目标方法
     */
    override fun getExecutorQualifier(method: Method): String {
        return ""
    }

}