package com.wanna.framework.scheduling.annotation

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.Pointcut
import com.wanna.framework.aop.interceptor.AsyncUncaughtExceptionHandler
import com.wanna.framework.aop.support.AbstractPointcutAdvisor
import com.wanna.framework.aop.support.StaticMethodMatcherPointcut
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Method
import java.util.concurrent.Executor
import java.util.function.Supplier

/**
 * 对@Async注解的增强Advisor, 支持去匹配类上/方法上的@Async注解, 去使用AnnotationAsyncExecutionInterceptor
 * 去进行拦截该异步方法, 从而完成异步方法的增强
 *
 * @see AnnotationAsyncExecutionInterceptor
 */
open class AsyncAnnotationAdvisor() : AbstractPointcutAdvisor(), BeanFactoryAware {

    /**
     * advice, 用于去拦截@Async注解的执行
     */
    private var advice: Advice? = null

    /**
     * pointcut, 提供对于@Async注解的匹配
     */
    private var pointcut: Pointcut? = null

    /**
     * beanFactory
     */
    private var beanFactory: BeanFactory? = null

    /**
     * 异步任务的ExceptionHandler
     */
    private var exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>? = null

    constructor(executor: Supplier<Executor>?, exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?) : this() {
        this.exceptionHandler = exceptionHandler
        val asyncAnnotationTypes: Set<Class<out Annotation>> = linkedSetOf(Async::class.java)  // 设置支持的@Async注解
        this.advice = this.buildAdvice(executor, exceptionHandler)  // 构建Advice
        this.pointcut = this.buildPointcut(asyncAnnotationTypes)  // 构建Pointcut
    }

    /**
     * 需要设置自定义的@Async注解类型
     *
     * @param asyncAnnotationType 你想要使用的@Async注解类型
     */
    open fun setAsyncAnnotationType(asyncAnnotationType: Class<out Annotation>) {
        val asyncAnnotationTypes: Set<Class<out Annotation>> = linkedSetOf(asyncAnnotationType)
        this.pointcut = this.buildPointcut(asyncAnnotationTypes)  // rebuild pointcut
    }

    /**
     * 构建增强的@Async注解的Advisor
     *
     * @param executor 要使用的Executor的Supplier
     * @param exceptionHandler 要使用的ExceptionHandler
     */
    protected open fun buildAdvice(
        executor: Supplier<Executor>?, exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?
    ): Advice {
        return AnnotationAsyncExecutionInterceptor(executor, exceptionHandler)
    }

    /**
     * 构建Pointcut, 去匹配一个类/方法上的@Async注解
     *
     * @param asyncAnnotationTypes 要使用的@Async注解列表?
     * @return Pointcut
     */
    protected open fun buildPointcut(asyncAnnotationTypes: Set<Class<out Annotation>>): Pointcut {
        return object : StaticMethodMatcherPointcut() {
            override fun matches(method: Method, targetClass: Class<*>): Boolean {
                asyncAnnotationTypes.forEach {
                    if (AnnotatedElementUtils.hasAnnotation(targetClass, it)) {
                        return true
                    }
                    if (AnnotatedElementUtils.hasAnnotation(method, it)) {
                        return true
                    }
                }
                return false
            }
        }
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
        if (this.advice is BeanFactoryAware) {
            (this.advice as BeanFactoryAware).setBeanFactory(beanFactory)
        }
    }

    override fun getAdvice(): Advice =
        advice ?: throw IllegalStateException("AsyncAnnotationAdvisor的Advice不能为空")

    override fun getPointcut(): Pointcut =
        pointcut ?: throw IllegalStateException("AsyncAnnotationAdvisor的Pointcut不能为空")
}