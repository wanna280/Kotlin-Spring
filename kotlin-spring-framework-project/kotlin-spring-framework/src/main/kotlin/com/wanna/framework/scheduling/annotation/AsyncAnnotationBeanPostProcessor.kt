package com.wanna.framework.scheduling.annotation

import com.wanna.framework.aop.framework.AbstractBeanFactoryAwareAdvisingPostProcessor
import com.wanna.framework.aop.interceptor.AsyncUncaughtExceptionHandler
import com.wanna.framework.beans.factory.BeanFactory
import java.util.concurrent.Executor
import java.util.function.Supplier

/**
 * 为@Async注解提供支持的BeanPostProcessor, 负责去完成Spring Aop代理的创建; 
 * 这个类的父类当中, 支持使用给定的Advisor去对特定的Bean去进行代理, 我们需要做的
 * 就是新增加一个自定义的处理@Async注解的Advisor, 设置到父类的advisor字段当中
 *
 * @see com.wanna.framework.aop.framework.AbstractAdvisingBeanPostProcessor.advisor
 */
open class AsyncAnnotationBeanPostProcessor : AbstractBeanFactoryAwareAdvisingPostProcessor() {

    // Executor, 提供线程池去进行异步的执行
    private var executor: Supplier<Executor>? = null

    // 异步任务的ExceptionHandler
    private var exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>? = null

    // 异步注解类型, 外部去进行自定义(如果不自定义, 将会使用@Async注解)
    private var asyncAnnotationType: Class<out Annotation>? = null

    open fun setExecutor(executor: Supplier<Executor>?) {
        this.executor = executor
    }

    open fun setExceptionHandler(exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>?) {
        this.exceptionHandler = exceptionHandler
    }

    /**
     * 设置自定义的@Async注解, 替换掉默认的@Async注解
     *
     * @param asyncAnnotationType 你想要使用的@Async注解类型
     */
    open fun setAsyncAnnotationType(asyncAnnotationType: Class<out Annotation>) {
        this.asyncAnnotationType = asyncAnnotationType
    }

    /**
     * 在setBeanFactory的callback当中, 设置父类的advisor为处理@Async的Advisor
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        super.setBeanFactory(beanFactory)
        val advisor = AsyncAnnotationAdvisor(executor, exceptionHandler)
        advisor.setBeanFactory(beanFactory)  // set BeanFactory

        // 如果有自定义的@Async的话, 那么需要apply给Advisor
        if (this.asyncAnnotationType != null) {
            advisor.setAsyncAnnotationType(this.asyncAnnotationType!!)
        }

        // set Advisor to use
        this.advisor = advisor
    }
}