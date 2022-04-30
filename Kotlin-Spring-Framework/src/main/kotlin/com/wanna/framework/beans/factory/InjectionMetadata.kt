package com.wanna.framework.beans.factory

import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.method.PropertyValues
import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * 它是封装一个类当中要进行注入的元素列表
 */
open class InjectionMetadata(val targetClass: Class<*>, private val elements: Collection<InjectedElement>) {

    /**
     * 遍历所有要进行注入的元素，去进行依赖的注入
     */
    fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
        elements.forEach { element ->
            element.inject(bean, beanName, pvs)
        }
    }

    companion object {
        /**
         * 静态工厂方法，提供构建InjectMetadata的方式
         */
        @JvmStatic
        fun forElements(targetClass: Class<*>, elements: Collection<InjectedElement>) =
            InjectionMetadata(targetClass, elements)
    }


    /**
     * 要进行注入的元素
     */
    abstract class InjectedElement(_member: Member) {

        // 要进行注入的成员，方法/字段/构造器都是Member
        private val member: Member = _member

        // 元素是否是字段？
        private val isField = _member is Field

        open fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
            val resourceToInject = getResourceToInject(bean, beanName)
            if (isField) {
                ReflectionUtils.makeAccessiable(member as Field)
                ReflectionUtils.setField(member, bean, resourceToInject)
            } else {
                ReflectionUtils.makeAccessiable(member as Method)
                ReflectionUtils.invokeMethod(member, bean, resourceToInject as Array<Any?>)
            }
        }

        /**
         * 获取资源去进行Inject
         */
        protected open fun getResourceToInject(bean: Any, beanName: String): Any? {
            return null
        }
    }
}