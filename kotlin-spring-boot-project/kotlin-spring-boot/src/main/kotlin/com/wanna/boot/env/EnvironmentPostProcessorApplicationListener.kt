package com.wanna.boot.env

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.boot.context.event.ApplicationFailedEvent
import com.wanna.boot.context.event.ApplicationPreparedEvent
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个监听Spring应用的环境的相关事件的[ApplicationListener], 它支持在SpringApplication启动当中,
 * 对环境的相关事件去进行处理, 并将配置文件的相关信息去merge到Environment当中去
 *
 * @see ApplicationEnvironmentPreparedEvent
 * @see ApplicationListener
 */
open class EnvironmentPostProcessorApplicationListener : SmartApplicationListener {

    companion object {

        /**
         * !!!!!处理Environment的ApplicationListener应该有最高优先级,
         * 保证配置文件可以最快被加载到, 这样后续的别的Environment的相关的时间,
         * 才能获取到Environment当中的配置信息, 不然会导致配置文件当中的配置信息不会生肖
         */
        const val DEFAULT_ORDER = Ordered.ORDER_HIGHEST + 10
    }

    /**
     * Order优先级, 默认为最高优先级
     */
    private var order = DEFAULT_ORDER

    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder(): Int = order

    override fun onApplicationEvent(event: ApplicationEvent) {
        if (event is ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent(event)
        }
    }

    override fun supportEventType(eventType: Class<out ApplicationEvent>): Boolean {
        return ClassUtils.isAssignFrom(ApplicationEnvironmentPreparedEvent::class.java, eventType)
                || ClassUtils.isAssignFrom(ApplicationPreparedEvent::class.java, eventType)
                || ClassUtils.isAssignFrom(ApplicationFailedEvent::class.java, eventType)
    }

    /**
     * 处理环境准备好的事件, 遍历所有的EnvironmentPostProcessor去完成后置处理
     *
     * @param event event
     */
    open fun onApplicationEnvironmentPreparedEvent(event: ApplicationEnvironmentPreparedEvent) {
        for (environmentPostProcessor in getEnvironmentPostProcessors()) {
            environmentPostProcessor.postProcessEnvironment(event.environment, event.application)
        }
    }

    /**
     * 从SpringFactories当中去获取到环境处理器, 去对环境去进行处理
     *
     * @return SpringFactories当中的所有的EnvironmentPostProcessor
     */
    open fun getEnvironmentPostProcessors(): List<EnvironmentPostProcessor> {
        return SpringFactoriesLoader.loadFactories(EnvironmentPostProcessor::class.java)
    }
}