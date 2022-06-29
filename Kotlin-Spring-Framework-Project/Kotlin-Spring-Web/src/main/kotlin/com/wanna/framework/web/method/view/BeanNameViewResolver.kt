package com.wanna.framework.web.method.view

import com.wanna.framework.core.Ordered
import com.wanna.framework.web.context.WebApplicationObjectSupport
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.ui.View

/**
 * 基于BeanName的ViewResolver
 */
open class BeanNameViewResolver : ViewResolver, WebApplicationObjectSupport(), Ordered {

    private var order = Ordered.ORDER_LOWEST

    override fun getOrder() = this.order

    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun resolveView(viewName: String): View? {
        if (!obtainApplicationContext().containsBeanDefinition(viewName)) {
            return null
        }
        if (!obtainApplicationContext().isTypeMatch(viewName, View::class.java)) {
            return null
        }
        return obtainApplicationContext().getBean(viewName, View::class.java)
    }
}