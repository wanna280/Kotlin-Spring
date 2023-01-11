package com.wanna.boot.builder

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.ContextRefreshedEvent
import com.wanna.framework.core.Ordered

/**
 * 为一个child [ApplicationContext]去设置parent [ApplicationContext]的[ApplicationContextInitializer],
 * 与此同时, 在设置child [ApplicationContext]刷新完成时, 还会触发[ParentContextAvailableEvent]事件,
 * 用来去告知所有的Listener, parent Context已经可用, 并且当前[ApplicationContext]存在有parent Context
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/11
 *
 * @param parent parent ApplicationContext
 */
open class ParentContextApplicationContextInitializer(private val parent: ConfigurableApplicationContext) :
    ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    /**
     * Order
     */
    private var order: Int = Ordered.ORDER_HIGHEST

    override fun getOrder(): Int = order

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * 当child [ApplicationContext]进行初始化时, 为它去设置parent [ApplicationContext]
     *
     * @param applicationContext child ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        if (this.parent != applicationContext) {
            applicationContext.setParent(parent)
            applicationContext.addApplicationListener(EventPublisher)
        }
    }

    /**
     * EventPublisher单例对象, 监听child ApplicationContext的[ContextRefreshedEvent]事件;
     * 当child ApplicationContext已经刷新完毕时, 发布[ParentContextAvailableEvent]事件.
     */
    private object EventPublisher : ApplicationListener<ContextRefreshedEvent>, Ordered {
        override fun onApplicationEvent(event: ContextRefreshedEvent) {
            val applicationContext = event.applicationContext
            if (applicationContext is ConfigurableApplicationContext && applicationContext == event.source) {
                applicationContext.publishEvent(ParentContextAvailableEvent(applicationContext))
            }
        }

        override fun getOrder(): Int = Ordered.ORDER_HIGHEST
    }

    /**
     * parentContext已经可用的事件, 在childContext刷新完成时, 自动触发事件
     *
     * @param applicationContext child ApplicationContext
     */
    class ParentContextAvailableEvent(applicationContext: ConfigurableApplicationContext) :
        ApplicationEvent(applicationContext) {
        fun getApplicationContext(): ConfigurableApplicationContext = source as ConfigurableApplicationContext
    }
}