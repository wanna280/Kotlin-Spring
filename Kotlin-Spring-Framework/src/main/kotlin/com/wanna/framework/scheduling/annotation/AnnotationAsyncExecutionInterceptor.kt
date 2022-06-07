package com.wanna.framework.scheduling.annotation

import com.wanna.framework.aop.interceptor.AsyncExecutionInterceptor
import com.wanna.framework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Method
import java.util.concurrent.Executor
import java.util.function.Supplier

/**
 * 提供异步执行的Execution的MethodInterceptor，负责拦截目标@Async方法的执行；
 * 在父类当中都已经提供了模板方法了，在这个类当中，主要是提供@Async的qualifier的获取方式
 *
 * @param executor 想要使用的默认线程池，在@Async当中配置的和beanFactory当中都找不到默认线程池时去进行使用
 * @param exceptionHandler ExceptionHandler
 */
open class AnnotationAsyncExecutionInterceptor(
    executor: Supplier<Executor>?, exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?
) : AsyncExecutionInterceptor(executor, exceptionHandler) {

    override fun getExecutorQualifier(method: Method): String {
        // 1.尝试从方法上去寻找@Async注解
        var async = AnnotatedElementUtils.getMergedAnnotation(method, Async::class.java)

        // 2.尝试去类上寻找@Async注解
        if (async == null) {
            async = AnnotatedElementUtils.getMergedAnnotation(method.declaringClass, Async::class.java)
        }
        return async?.value ?: ""
    }
}