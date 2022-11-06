package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.ResolvableType
import java.lang.reflect.Method

/**
 * 将一个标注了[EventListener]注解的方法, 去转换成为一个[ApplicationListener]的适配器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/7
 */
open class ApplicationListenerMethodAdapter(val beanName: String, val targetClass: Class<*>, val method: Method) :
    GenericApplicationListener {

    override fun onApplicationEvent(event: ApplicationEvent) {
        // TODO
    }

    override fun supportsEventType(type: ResolvableType): Boolean {
        // TODO
        return false
    }

    open fun init(context: ApplicationContext) {

    }
}