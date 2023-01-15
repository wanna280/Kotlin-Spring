package com.wanna.framework.context.event

import com.wanna.framework.core.Ordered

/**
 * 这是一个智能的ApplicationListener, 能够去判断支持的事件类型; 
 * 在Spring4.2之后, 将会采用GenericApplicationListener去代替, 因为它支持泛型的处理
 *
 * @see GenericApplicationListener
 */
interface SmartApplicationListener : ApplicationListener<ApplicationEvent>, Ordered {

    /**
     * 是否支持去处理这样的事件类型？只有支持处理该类型的事件, 才会将事件交给当前监听器去进行处理
     *
     * @param eventType 事件类型
     * @return 如果支持处理该事件类型, return true; 不支持则return false
     */
    fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean

    /**
     * 获取当前的ApplicationListener的优先级
     *
     * @return 优先级(默认为最低的优先级)
     */
    override fun getOrder(): Int = Ordered.ORDER_LOWEST
}