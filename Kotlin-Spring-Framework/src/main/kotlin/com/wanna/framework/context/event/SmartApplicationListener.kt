package com.wanna.framework.context.event

import com.wanna.framework.beans.annotations.Ordered

/**
 * 这是一个智能的ApplicationListener，能够去判断支持的事件类型；
 * 在Spring4.2之后，将会采用GenericApplicationListener去代替，因为它支持泛型的处理
 */
interface SmartApplicationListener : ApplicationListener<ApplicationEvent>, Ordered {
    fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean
    override fun getOrder(): Int = Ordered.ORDER_LOWEST
}