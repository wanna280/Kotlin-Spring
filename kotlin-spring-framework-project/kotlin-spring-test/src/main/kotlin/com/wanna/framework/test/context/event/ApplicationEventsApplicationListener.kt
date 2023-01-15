package com.wanna.framework.test.context.event

import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener

/**
 * [ApplicationEvents]的[ApplicationListener], 将Test方法执行过程当中的所有的事件收集到[ApplicationEventsHolder]当中; 
 * 对于不在Test方法执行过程当中的话, 那么不会被注册到[ApplicationEventsHolder]当中, 因为此时无法根据当前线程去获取到[ApplicationEvents].
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @see ApplicationEventsTestExecutionListener
 * @see RecordApplicationEvents
 * @see ApplicationEvents
 * @see DefaultApplicationEvents
 * @see ApplicationEventsHolder
 */
class ApplicationEventsApplicationListener : ApplicationListener<ApplicationEvent> {

    /**
     * 监听[ApplicationEvent]事件, 收集到[ApplicationEventsHolder]当中
     *
     * @param event ApplicationEvent
     */
    override fun onApplicationEvent(event: ApplicationEvent) {
        val applicationEvents = ApplicationEventsHolder.getApplicationEvents()

        // 如果ApplicationEvents不为null, 那么才需要将当前事件去进行注册到ApplicationEvents当中
        if (applicationEvents is DefaultApplicationEvents) {
            applicationEvents.addEvent(event)
        }
    }
}