package com.wanna.framework.context.event

import java.lang.reflect.Method

interface EventListenerFactory {
    fun supportsMethod(method: Method): Boolean

    fun <T> createApplicationListener(beanName: String, type: Class<T>, method: Method): ApplicationListener<*>
}