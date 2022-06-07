package com.wanna.framework.beans

import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Field


/**
 * 它为ConfigurablePropertyAccessor提供了典型的模板方法实现；
 *
 * 它也提供了基于ConversionService和PropertyEditor去进行类型转换的功能
 *
 * @see PropertyAccessor
 */
abstract class AbstractNestablePropertyAccessor : AbstractPropertyAccessor() {

    private var wrappedObject: Any? = null

    open fun setWrappedInstance(wrappedObject: Any) {
        this.wrappedObject = wrappedObject
    }

    open fun getWrappedInstance(): Any {
        return wrappedObject ?: IllegalStateException("无法获取到实例，请先完成beanInstance的初始化工作")
    }

    open fun getWrappedClass(): Class<*> {
        return getWrappedInstance()::class.java
    }

    override fun setPropertyValue(name: String, value: Any?) {
        // TODO 这里应该进行更多的类型判断和转换工作

        // 如果找不到字段直接return
        val field = getField(name) ?: return
        ReflectionUtils.makeAccessible(field)
        if (value is Collection<*> && !ClassUtils.isAssignFrom(Collection::class.java, field.type)) {
            val targetToInject: Any? = if (value.isNotEmpty()) value.iterator().next() else null
            ReflectionUtils.setField(field, getWrappedInstance(), convertIfNecessary(targetToInject, field.type))
            return
        }
        ReflectionUtils.setField(field, getWrappedInstance(), convertIfNecessary(value, field.type))
    }

    private fun processKeyedProperty(propertyValue: PropertyValue) {

    }

    /**
     * beanClass以及它的父类当前去获取指定name的字段，如果没有找到，return null；
     * 如果找到了多个，优先返回子类当中的字段
     */
    private fun getField(name: String): Field? {
        val result: ArrayList<Field> = ArrayList()
        ReflectionUtils.doWithFields(getWrappedClass()) {
            if (name == it.name) {
                result += it
            }
        }
        return if (result.isEmpty()) null else result[0]
    }

    override fun getPropertyValue(name: String): Any? {
        // 如果找不到字段那么直接return null
        val field = getField(name) ?: return null
        ReflectionUtils.makeAccessible(field)
        return ReflectionUtils.getField(field, getWrappedInstance())
    }

}