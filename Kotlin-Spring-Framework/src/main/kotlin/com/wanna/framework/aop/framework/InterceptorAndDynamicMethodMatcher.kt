package com.wanna.framework.aop.framework

import com.wanna.framework.aop.MethodMatcher
import com.wanna.framework.aop.intercept.MethodInterceptor

/**
 * 它包装了Interceptor和运行时的方法匹配的MethodMatcher
 */
class InterceptorAndDynamicMethodMatcher(val interceptor: MethodInterceptor, val methodMatcher: MethodMatcher)