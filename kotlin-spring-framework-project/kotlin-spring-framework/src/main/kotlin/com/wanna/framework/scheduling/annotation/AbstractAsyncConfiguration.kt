package com.wanna.framework.scheduling.annotation

import com.wanna.framework.aop.interceptor.AsyncUncaughtExceptionHandler
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.ImportAware
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.type.AnnotationMetadata
import java.util.concurrent.Executor
import java.util.function.Supplier

/**
 * 为异步提供配置的配置类
 */
abstract class AbstractAsyncConfiguration : ImportAware {

    /**
     * 描述@EnableAsync注解的属性
     */
    protected var annotationAttributes: MergedAnnotation<*>? = null

    // 执行异步方法的默认Executor
    protected var executor: Supplier<Executor>? = null

    // 执行异步方法过程中遇到没有捕获的异常，应该怎么去处理？
    protected var exceptionHandler: Supplier<AsyncUncaughtExceptionHandler>? = null

    /**
     * Spring自动注入标注@EnableAsync注解的类的元信息，方便去获取到@EnableAsync注解的相关属性
     *
     * @param annotationMetadata 注解元信息
     */
    override fun setImportMetadata(annotationMetadata: AnnotationMetadata) {
        val attributes = annotationMetadata.getAnnotations().get(EnableAsync::class.java)
        if (!attributes.present) {
            throw IllegalStateException("Cannot find @EnableAsync Annotation from ${annotationMetadata.getClassName()}")
        }
        this.annotationAttributes = attributes
    }

    /**
     * 自动注入容器当中的AsyncConfigurer，对支持异步的Executor和ExceptionHandler去进行配置
     *
     * @param configurers 容器中的AsyncConfigurer列表
     */
    @Autowired(required = false)
    open fun setConfigurers(configurers: Collection<AsyncConfigurer>) {
        if (configurers.isEmpty()) {
            return
        }
        if (configurers.size > 1) {
            throw IllegalStateException("不应该有多个AsyncConfigurer")
        }
        val configurer = configurers.iterator().next()
        val executor = configurer.getAsyncExecutor()
        val exceptionHandler = configurer.getAsyncUncaughtExceptionHandler()
        if (executor != null) {
            this.executor = Supplier { executor }
        }
        if (exceptionHandler != null) {
            this.exceptionHandler = Supplier { exceptionHandler }
        }
    }
}