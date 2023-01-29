package com.wanna.framework.aop.framework

import com.wanna.framework.beans.factory.BeanClassLoaderAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个为ProxyProcessor提供支持的组件
 */
open class ProxyProcessorSupport : Ordered, ProxyConfig(), AopInfrastructureBean, BeanClassLoaderAware {
    private var order: Int = Ordered.ORDER_LOWEST

    private var proxyClassLoader: ClassLoader = ClassUtils.getDefaultClassLoader()

    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder(): Int {
        return this.order
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.proxyClassLoader = classLoader
    }

    open fun getProxyClassLoader(): ClassLoader? = proxyClassLoader
}