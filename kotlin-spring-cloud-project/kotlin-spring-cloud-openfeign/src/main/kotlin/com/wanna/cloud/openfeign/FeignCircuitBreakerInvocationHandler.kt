package com.wanna.cloud.openfeign

import feign.InvocationHandlerFactory.MethodHandler
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Feign的CircuitBreaker的InvocationHandler, 用来反射执行目标方法
 */
class FeignCircuitBreakerInvocationHandler(
    private val target: feign.Target<*>,
    private val fallbackFactory: FallbackFactory<*>?,
    private val dispatch: Map<Method, MethodHandler>
) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any {
        return when (method.name) {
            "hashCode" -> hashCode()
            "toString" -> toString()
            "equals" -> equals(args[0])
            else -> dispatch[method]!!.invoke(args)
        }
    }
}