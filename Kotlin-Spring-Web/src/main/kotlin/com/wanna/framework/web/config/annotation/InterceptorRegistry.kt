package com.wanna.framework.web.config.annotation

import com.wanna.framework.core.comparator.OrderComparator
import com.wanna.framework.web.HandlerInterceptor

/**
 * 这是一个拦截器的注册中心
 */
open class InterceptorRegistry {
    companion object {
        private val INTERCEPTOR_COMPARATOR = Comparator<InterceptorRegistration> { o1, o2 ->
            OrderComparator.INSTANCE.compare(
                o1.interceptor, o2.interceptor
            )
        }
    }


    // 拦截器列表
    private val interceptors = ArrayList<InterceptorRegistration>()

    /**
     * 添加一个拦截器到注解中心当中
     */
    open fun addInterceptor(interceptor: HandlerInterceptor) {
        interceptors += InterceptorRegistration(interceptor)
    }

    open fun getInterceptors(): List<Any> {
        return interceptors.sortedWith(INTERCEPTOR_COMPARATOR).toList()
    }
}