package com.wanna.framework.aop.framework

import java.lang.reflect.Method

/**
 * 这是一个根据Advised和目标方法，去生成AOP的拦截器链的工厂
 */
interface AdvisorChainFactory {
    fun getInterceptorsAndDynamicInterceptionAdvice(advised: Advised, method: Method, targetClass: Class<*>?): List<Any>
}