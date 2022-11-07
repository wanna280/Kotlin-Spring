package com.wanna.framework.context.event

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Method

/**
 * 默认的EventListenerFactory的实现，负责处理@EventListener注解标注的方法
 *
 * @see EventListenerFactory
 * @see EventListener
 */
open class DefaultEventListenerFactory : EventListenerFactory {

    /**
     * 是否支持当前方法？只要该方法标注了@EventListener注解，那么就支持去进行处理
     *
     * @param method 想要匹配的方法
     */
    override fun supportsMethod(method: Method) = AnnotatedElementUtils.isAnnotated(method, EventListener::class.java)

    /**
     * 如果支持处理该方法的话，那么需要根据该方法去创建一个[ApplicationListener]
     *
     * @param beanName beanName
     * @param type beanClass
     * @param method method
     * @return 包装之后的ApplicationListener
     */
    override fun <T> createApplicationListener(
        beanName: String,
        type: Class<T>,
        method: Method
    ): ApplicationListener<*> {
        return ApplicationListenerMethodAdapter(beanName, type, method)
    }
}