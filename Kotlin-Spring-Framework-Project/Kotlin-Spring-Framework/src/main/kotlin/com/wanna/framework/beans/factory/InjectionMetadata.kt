package com.wanna.framework.beans.factory

import com.wanna.framework.beans.PropertyValues
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
    open fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
        elements.forEach { element ->
            element.inject(bean, beanName, pvs)
        }
    }

    companion object {
        /**
         * 静态工厂方法，提供构建InjectMetadata的方式
         */
        @JvmStatic
        fun forElements(targetClass: Class<*>, elements: Collection<InjectedElement>): InjectionMetadata {
            return InjectionMetadata(targetClass, elements)
        }

        // 这是一个空的InjectMetadata的实例
        val EMPTY = object : InjectionMetadata(Any::class.java, emptyList()) {
            override fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {}
        }
    }


    /**
     * 它描述了一个要去进行自动注入(Autowire)的元素，可以是一个方法/字段
     *
     * @param _member 方法/字段
     */
    abstract class InjectedElement(_member: Member) {
        // 要进行注入的成员，方法/字段/构造器都是Member的子类
        val member: Member = _member
        // 元素是否是字段？
        val isField = _member is Field

        /**
         * 对该元素(方法/字段)去完成Autowire注入
         *
         * @param bean bean
         * @param beanName beanName
         * @param pvs PropertyValues
         */
        @Suppress("UNCHECKED_CAST")
        open fun inject(bean: Any, beanName: String, pvs: PropertyValues?) {
            val resourceToInject = getResourceToInject(bean, beanName)
            if (isField) {
                ReflectionUtils.makeAccessible(member as Field)
                ReflectionUtils.setField(member, bean, resourceToInject)
            } else {
                ReflectionUtils.makeAccessible(member as Method)
                ReflectionUtils.invokeMethod(member, bean, resourceToInject as Array<Any?>)
            }
        }

        /**
         * 获取资源去进行Inject
         *
         * @param bean bean
         * @param beanName beanName
         */
        protected open fun getResourceToInject(bean: Any, beanName: String): Any? {
            return null
        }
    }
}