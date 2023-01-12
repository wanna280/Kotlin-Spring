package com.wanna.boot.context.config

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.framework.context.ApplicationContextException
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.SimpleApplicationEventMulticaster
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.util.ArrayList

/**
 * 当SpringBoot的[ApplicationEnvironmentPreparedEvent]事件发布时,
 * 拿出来所有的通过"context.listener.classes"([PROPERTY_NAME])去配置的Listener, 去进行事件的触发
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/11
 *
 * @see PROPERTY_NAME
 */
open class DelegatingApplicationListener : ApplicationListener<ApplicationEvent>, Ordered {

    companion object {
        private const val PROPERTY_NAME = "context.listener.classes"
    }

    /**
     * Order
     */
    private var order: Int = 0

    /**
     * 事件广播器
     */
    @Nullable
    private var multicaster: SimpleApplicationEventMulticaster? = null

    override fun onApplicationEvent(event: ApplicationEvent) {
        if (event is ApplicationEnvironmentPreparedEvent) {
            val listeners = getListeners(event.environment)
            if (listeners.isEmpty()) {
                return
            }
            multicaster = SimpleApplicationEventMulticaster()
            for (listener in listeners) {
                multicaster?.addApplicationListener(listener)
            }
        }
        multicaster?.multicastEvent(event)
    }

    /**
     * 从[ConfigurableEnvironment]当中根据[PROPERTY_NAME]去获取到配置的所有的[ApplicationListener]
     *
     * @param environment Environment
     * @return 从Environment当中, 根据[PROPERTY_NAME]去探测到的ApplicationListener列表
     */
    private fun getListeners(environment: ConfigurableEnvironment): List<ApplicationListener<ApplicationEvent>> {
        val classNames = environment.getProperty(PROPERTY_NAME)

        val listeners = ArrayList<ApplicationListener<ApplicationEvent>>()
        if (StringUtils.hasText(classNames)) {
            val listenerClassNames = StringUtils.commaDelimitedListToStringArray(classNames)
            for (className in listenerClassNames) {
                try {
                    val listenerClass = ClassUtils.forName<ApplicationListener<ApplicationEvent>>(
                        className,
                        ClassUtils.getDefaultClassLoader()
                    )
                    if (ClassUtils.isAssignFrom(ApplicationListener::class.java, listenerClass)) {
                        throw IllegalStateException("class [$className] must implement ApplicationListener")
                    }
                    listeners += BeanUtils.instantiateClass(listenerClass)
                } catch (ex: ClassNotFoundException) {
                    throw ApplicationContextException("Failed to load context listener class [$className]", ex)
                }
            }
        }

        // sort
        AnnotationAwareOrderComparator.sort(listeners)
        return listeners
    }

    override fun getOrder(): Int = order

    open fun setOrder(order: Int) {
        this.order = order
    }
}