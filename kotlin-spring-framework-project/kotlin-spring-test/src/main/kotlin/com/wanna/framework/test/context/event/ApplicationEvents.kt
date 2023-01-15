package com.wanna.framework.test.context.event

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.test.context.TestExecutionListener
import com.wanna.framework.test.context.TestExecutionListeners
import java.util.stream.Stream

/**
 * 记录在执行单个Test方法时触发的[ApplicationEvent]事件列表.
 *
 * 要想将[ApplicationEvents]应用给你的Test应用, 需要做如下的这些事情：
 * * 1.确保你的TestClass标注(或者间接通过meta标注)了[RecordApplicationEvents]注解
 * * 2.确保[ApplicationEventsTestExecutionListener]被注册.(Note: 虽然默认情况下是已经被默认注册了,
 * 但是如果你使用了[TestExecutionListeners]注解去自定义[TestExecutionListener]的话, 默认的就会失效,
 * 这时就需要你去进行手动注册[ApplicationEventsTestExecutionListener], 不然不会生效).
 * * 3.通过[Autowired]注解标注在TestClass的一个类型为[ApplicationEvents]的字段上去进行自动注入,
 * 并且在你的Test方法或者是生命周期方法当中去使用[ApplicationEvents]
 *
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @see RecordApplicationEvents
 * @see ApplicationEvent
 * @see ApplicationEventsTestExecutionListener
 */
interface ApplicationEvents {

    /**
     * 获取在Test执行过程当中触发的[ApplicationEvent]事件列表
     *
     * @return Stream of ApplicationEvent
     */
    fun stream(): Stream<ApplicationEvent>

    /**
     * 获取在Test执行过程当中触发的指定类型的[ApplicationEvent]事件列表
     *
     * @param type 想要获取的[ApplicationEvent]的类型或者是Payload的类型
     * @param T 想要获取的[ApplicationEvent]的类型或者是Payload的类型
     * @return Stream of T(ApplicationEvent/PayLoad Of PayloadApplicationEvent)
     */
    fun <T> stream(type: Class<T>): Stream<T>

    /**
     * 清除这个[ApplicationEvents]当中的所有的已经记录的[ApplicationEvent]
     */
    fun clear()
}