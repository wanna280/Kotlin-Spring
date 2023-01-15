package com.wanna.framework.context.event

import com.wanna.framework.core.Ordered
import com.wanna.framework.core.ResolvableType

/**
 * 这是一个支持泛型的EventType, 它和SmartApplicationListener类似;
 * 推荐使用GenericApplicationListener去替代之前的SmartApplicationListener;
 * 因为GenericApplicationListener它传递的eventType是ResolvableType, 支持去解析泛型类型
 *
 * @see SmartApplicationListener
 */
interface GenericApplicationListener : SmartApplicationListener, Ordered {
    /**
     * 是否支持去处理这样的事件类型？
     *
     * @param type 事件类型(ResolvableType), 支持去进行泛型的解析
     * @return 如果支持处理该事件类型, return true; 不支持则return false
     */
    fun supportsEventType(type: ResolvableType): Boolean

    /**
     * 是否支持去进行处理这样的事件类型?(我们直接将它去转换成为调用ResolvableType的重载方法)
     *
     * @param eventType 事件类型
     * @return 如果支持处理该事件类型, return true; 不支持则return false
     */
    override fun supportEventType(eventType: Class<out ApplicationEvent>) =
        supportsEventType(ResolvableType.forClass(eventType))

    /**
     * 获取当前的ApplicationListener的优先级
     *
     * @return 优先级(默认为最低的优先级)
     */
    override fun getOrder() = Ordered.ORDER_LOWEST

}