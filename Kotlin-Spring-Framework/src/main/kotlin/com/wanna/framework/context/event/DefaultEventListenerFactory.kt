package com.wanna.framework.context.event

import java.lang.reflect.Method

open class DefaultEventListenerFactory : EventListenerFactory {
    override fun supportsMethod(method: Method): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> createApplicationListener(
        beanName: String,
        type: Class<T>,
        method: Method
    ): ApplicationListener<*> {
        TODO("Not yet implemented")
    }
}