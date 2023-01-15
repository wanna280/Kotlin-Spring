package com.wanna.framework.scheduling.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.scheduling.config.TaskManagementConfigUtils

/**
 * 为@Async的方法提供代理的配置类导入一个BeanPostProcessor, 去将匹配@Async的Advisor应用给方法/类上标注了@Async注解的Bean;
 * 让标注@Async的方法可以通过Spring Aop的方式去进行代理完成异步的执行
 *
 * @see Async
 * @see EnableAsync
 * @see AsyncAnnotationBeanPostProcessor
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
open class ProxyAsyncConfiguration : AbstractAsyncConfiguration() {
    companion object {
        val DEFAULT_ANNOTATION = Async::class.java
    }

    /**
     * 给容器中导入一个处理@Async的BeanPostProcessor, 并处理@EnableAsync注解当中的相关属性, 该BeanPostProcessor会设置一个Advisor去匹配@Async注解;
     *
     * * 1.要使用的Advisor是AsyncAnnotationAdvisor
     * * 2.该Advisor当中要使用的Advice为AnnotationAsyncExecutionInterceptor
     * * 3.该Advisor的Pointcut用于去匹配类上/方法上的@Async注解
     *
     * @see AsyncAnnotationAdvisor
     * @see AnnotationAsyncExecutionInterceptor
     */
    @Bean(TaskManagementConfigUtils.ASYNC_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    open fun asyncAdvisor(): AsyncAnnotationBeanPostProcessor {
        val asyncAnnotationBeanPostProcessor = AsyncAnnotationBeanPostProcessor()
        asyncAnnotationBeanPostProcessor.setExecutor(executor)
        asyncAnnotationBeanPostProcessor.setExceptionHandler(exceptionHandler)
        // 解析@EnableAsync注解上的相关属性
        val attributes = annotationAttributes ?: throw IllegalStateException("无法从目标类上找到@EnableAsync注解")
        asyncAnnotationBeanPostProcessor.setOrder(attributes.getInt("order"))
        val annotation = attributes.getClass("annotation")
        if (annotation != DEFAULT_ANNOTATION) {
            @Suppress("UNCHECKED_CAST")
            asyncAnnotationBeanPostProcessor.setAsyncAnnotationType(annotation as Class<out Annotation>)
        }
        asyncAnnotationBeanPostProcessor.proxyTargetClass = attributes.getBoolean("proxyTargetClass")
        return asyncAnnotationBeanPostProcessor
    }
}