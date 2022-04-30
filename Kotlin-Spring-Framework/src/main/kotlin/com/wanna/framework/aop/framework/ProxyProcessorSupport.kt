package com.wanna.framework.aop.framework

import com.wanna.framework.core.Ordered

/**
 * 这是一个为ProxyProcessor提供支持的组件
 */
open class ProxyProcessorSupport() : Ordered, ProxyConfig() {
    private var order: Int = Ordered.ORDER_LOWEST

    fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder(): Int {
        return this.order
    }
}