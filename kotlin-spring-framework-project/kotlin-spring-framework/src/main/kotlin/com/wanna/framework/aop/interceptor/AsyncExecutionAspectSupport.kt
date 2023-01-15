package com.wanna.framework.aop.interceptor

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.core.task.AsyncTaskExecutor
import com.wanna.framework.core.task.TaskExecutor
import com.wanna.framework.core.task.support.TaskExecutorAdapter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.function.Supplier

/**
 * 提供异步方法的执行的Aspect的支持
 *
 * * 1.提供了获取了异步方法的线程池的方法
 * * 2.提供了提交给目标方法给线程池的方法
 */
abstract class AsyncExecutionAspectSupport(
    private val defaultExecutor: Supplier<Executor>?,
    exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?
) : BeanFactoryAware {
    companion object {
        const val DEFAULT_TASK_EXECUTOR_BEAN_NAME = "taskExecutor"  // beanName of TaskExecutor
        private val logger = LoggerFactory.getLogger(AsyncExecutionAspectSupport::class.java)
    }

    // beanFactory
    private var beanFactory: BeanFactory? = null

    // ExceptionHandler
    private var exceptionHandler: Supplier<AsyncUncaughtExceptionHandler> =
        Supplier { SimpleAsyncUncaughtExceptionHandler() }

    // 缓存一个异步方法对应的线程池
    private val executors = ConcurrentHashMap<Method, AsyncTaskExecutor>()

    // 从SpringBeanFactory当中获取到默认的Executor的Supplier
    private val defaultFactoryExecutor: Supplier<Executor?> = Supplier { getDefaultExecutor(beanFactory) }

    init {
        if (exceptionHandler != null) {
            this.exceptionHandler = exceptionHandler
        }
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 给定一个具体的方法, 决定出要使用哪个Executor去执行异步任务？
     *
     * @param method 目标方法
     */
    protected open fun determineAsyncExecutor(method: Method): AsyncTaskExecutor? {
        var executor = executors[method] // get Cache
        if (executor == null) {
            val qualifier = getExecutorQualifier(method)
            val taskExecutor: Executor?
            if (StringUtils.hasText(qualifier)) {
                taskExecutor = beanFactory?.getBean(qualifier, Executor::class.java)
            } else {
                if (this.defaultExecutor != null) {
                    taskExecutor = this.defaultExecutor.get()
                } else {
                    taskExecutor = this.defaultFactoryExecutor.get()
                }
            }
            if (taskExecutor != null) {
                executor = TaskExecutorAdapter(taskExecutor)
                executors[method] = executor // put Cache
            }
        }
        return executor
    }

    /**
     * 提交任务给线程池去执行任务, 并返回执行的结果
     *
     * @param task 要提交给线程池执行的任务(Callable)
     * @param executor 要使用的线程池
     * @param returnType 方法的返回值类型
     * @return 目标方法的返回值结果(支持CompletableFuture/Future, 别的类型不支持, 只能return null)
     */
    protected open fun doSubmit(task: Callable<Any?>, executor: AsyncTaskExecutor, returnType: Class<*>): Any? {
        // 如果要求返回CompletableFuture
        if (ClassUtils.isAssignFrom(CompletableFuture::class.java, returnType)) {
            return CompletableFuture.runAsync({ task.call() }, executor)
        }
        // 如果要求返回的是juc的Future
        if (ClassUtils.isAssignFrom(Future::class.java, returnType)) {
            return executor.submit(task)
        }
        executor.submit(task)
        return null
    }

    /**
     * 给定一个具体的方法, 获取它的Executor的Qualifier(beanName)
     *
     * @param method method
     * @return qualifierName
     */
    protected abstract fun getExecutorQualifier(method: Method): String

    /**
     * 尝试从BeanFactory当中去获取到默认的Executor
     *
     * @param beanFactory beanFactory
     * @return 如果beanFactory不为null, 并且beanFactory当中有Executor, 那么从beanFactory当中获取; 否则return null
     */
    protected open fun getDefaultExecutor(beanFactory: BeanFactory?): Executor? {
        if (beanFactory != null) {
            try {
                return beanFactory.getBean(TaskExecutor::class.java)
            } catch (ex: NoUniqueBeanDefinitionException) {
                logger.debug("无法从Spring BeanFactory当中找到唯一的TaskExecutor...")
                try {
                    return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor::class.java)
                } catch (ex2: NoSuchBeanDefinitionException) {
                    if (logger.isInfoEnabled) {
                        logger.info("Spring BeanFactory当中存在有多个Executor, 但是无法找到名为[$DEFAULT_TASK_EXECUTOR_BEAN_NAME]的Executor")
                    }
                }
            } catch (ex: NoSuchBeanDefinitionException) {
                logger.debug("无法从Spring BeanFactory当中找到一个的TaskExecutor...")
                try {
                    return beanFactory.getBean(DEFAULT_TASK_EXECUTOR_BEAN_NAME, Executor::class.java)
                } catch (ex2: NoSuchBeanDefinitionException) {
                    if (logger.isInfoEnabled) {
                        logger.info("Spring BeanFactory当中没有存在有beanName=[$DEFAULT_TASK_EXECUTOR_BEAN_NAME]的Executor")
                    }
                }
            }
        }
        return null
    }

    /**
     * 使用ExceptionHandler去处理异常情况...
     *
     * @param ex 要去处理的异常
     * @param method 目标方法
     * @param args 方法参数列表
     */
    protected open fun handleException(ex: Throwable, method: Method, vararg args: Any?) {
        try {
            exceptionHandler.get().handleException(ex, method, *args)
        } catch (ex2: Exception) {
            logger.error("ExceptionHandler处理目标方法[${method.toGenericString()}]失败",ex2)
        }
    }
}