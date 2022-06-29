package com.wanna.framework.web.config.annotation

import com.wanna.framework.web.HandlerInterceptor

/**
 * 一个拦截器的注册表项
 *
 * @param interceptor 要去进行注册的拦截器
 */
open class InterceptorRegistration(val interceptor: HandlerInterceptor) {

}