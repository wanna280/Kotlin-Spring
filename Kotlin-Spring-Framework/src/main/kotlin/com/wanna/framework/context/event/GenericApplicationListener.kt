package com.wanna.framework.context.event

import com.wanna.framework.core.Ordered
import com.wanna.framework.core.ResolvableType

/**
 * 这是一个支持泛型的EventType，它和SmartApplicationListener类似，推荐使用GenericApplicationListener去替代之前的SmartApplicationListener；
 * 因为GenericApplicationListener支持去解析泛型类型
 *
 * @see SmartApplicationListener
 */
interface GenericApplicationListener : ApplicationListener<ApplicationEvent>, Ordered {
    /**
     * 是否支持这样的事件类型？
     *
     * @param type 事件类型(ResolvableType)，支持去进行泛型的解析
     */
    fun supportsEventType(type: ResolvableType): Boolean

    override fun getOrder(): Int {
        return Ordered.ORDER_LOWEST
    }

}