package com.wanna.boot.env

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.boot.context.event.ApplicationFailedEvent
import com.wanna.boot.context.event.ApplicationPreparedEvent
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.SmartApplicationListener
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个环境的ApplicationListener，它支持在SpringApplication启动当中，对环境的相关事件去进行处理
 */
open class EnvironmentPostProcessorApplicationListener : SmartApplicationListener {
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
     * 处理环境准备好的事件，遍历所有的EnvironmentPostProcessor去完成后置处理
     *
     * @param event event
     */
    open fun onApplicationEnvironmentPreparedEvent(event: ApplicationEnvironmentPreparedEvent) {
        getEnvironmentPostProcessors().forEach {
            it.postProcessEnvironment(event.environment, event.application)
        }
    }

    /**
     * 从SpringFactories当中去获取到环境处理器，去对环境去进行处理
     */
    open fun getEnvironmentPostProcessors(): List<EnvironmentPostProcessor> {
        return SpringFactoriesLoader.loadFactories(EnvironmentPostProcessor::class.java)
    }
}