package com.wanna.framework.test.context.event

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.PayloadApplicationEvent
import java.util.stream.Stream

/**
 * [ApplicationEvents]的默认实现，记录在Test执行过程当中的所有的[ApplicationEvent]事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
class DefaultApplicationEvents : ApplicationEvents {

    /**
     * 维护所有的触发的[ApplicationEvent]事件列表
     */
    private val events = ArrayList<ApplicationEvent>()

    /**
     * 添加一个[ApplicationEvent]事件到当前[ApplicationEvents]当中来
     *
     * @param event 想要添加的[ApplicationEvent]
     */
    fun addEvent(event: ApplicationEvent) {
        this.events += event
    }

    /**
     * 获取在Test执行过程当中触发的[ApplicationEvent]事件列表
     *
     * @return Stream of ApplicationEvent
     */
    override fun stream(): Stream<ApplicationEvent> = events.stream()

    /**
     * 获取在Test执行过程当中触发的指定类型的[ApplicationEvent]事件列表
     *
     * @param type 想要获取的[ApplicationEvent]的类型或者是Payload的类型
     * @param T 想要获取的[ApplicationEvent]的类型或者是Payload的类型
     * @return Stream of T(ApplicationEvent/PayLoad Of PayloadApplicationEvent)
     */
    override fun <T> stream(type: Class<T>): Stream<T> =
        events.stream().map(this::unwrapPayloadEvent).filter(type::isInstance).map(type::cast)

    /**
     * 对于给定的[ApplicationEvent]去进行unwrap(对于PayloadApplicationEvent来说，我们需要拿到它的payload)
     *
     * @param source 原始的ApplicationEvent
     * @return unwrap之后的结果
     */
    private fun unwrapPayloadEvent(source: Any): Any {
        return if (source is PayloadApplicationEvent<*>) source.payload else source
    }

    /**
     * 清除这个[ApplicationEvents]当中的所有的已经记录的[ApplicationEvent]
     */
    override fun clear() = events.clear()
}