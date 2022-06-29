package com.wanna.framework.web.config.annotation

import com.wanna.framework.core.comparator.OrderComparator
import com.wanna.framework.web.HandlerInterceptor

/**
 * 这是一个拦截器的注册中心
 *
 * @see HandlerInterceptor
 * @see
 */
open class InterceptorRegistry {
    companion object {
        private val INTERCEPTOR_COMPARATOR = Comparator<InterceptorRegistration> { o1, o2 ->
            OrderComparator.INSTANCE.compare(o1.interceptor, o2.interceptor)
        }
    }

    // 拦截器列表(包装成为了拦截器的表项-InterceptorRegistration)
    private val interceptors = ArrayList<InterceptorRegistration>()

    /**
     * 添加一个拦截器到注解中心当中
     *
     * @param interceptor 要去进行添加的拦截器
     */
    open fun addInterceptor(interceptor: HandlerInterceptor) {
        interceptors += InterceptorRegistration(interceptor)
    }

    /**
     * 获取当前拦截器注册中心当中的拦截器列表
     *
     * @return 获取到当前拦截器的注册中心当中的拦截器列表
     */
    open fun getInterceptors(): List<Any> {
        return interceptors.sortedWith(INTERCEPTOR_COMPARATOR).map { it.interceptor }.toList()
    }
}