package com.wanna.framework.beans

import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Field

/**
 * BeanWrapper的具体实现，提供了属性的访问器，并组合了BeanFactory的TypeConverter，去完成Bean属性的类型的转换工作；
 * 它提供了SpringBean的BeanDefinition当中的PropertyValues(pvs)当中维护的所有的属性值的设置工作，在Spring的BeanDefinition当中，
 * 通过BeanDefinition去添加PropertyValue的方式，可以去实现属性的自动注入工作；
 */
open class BeanWrapperImpl(private var beanInstance: Any? = null) : BeanWrapper, AbstractPropertyAccessor() {

    /**
     * 提供后期设置beanInstance的方式
     */
    open fun setBeanInstance(instance: Any?) {
        this.beanInstance = instance
    }

    override fun getWrappedInstance(): Any {
        return beanInstance ?: IllegalStateException("无法获取到实例，请先完成beanInstance的初始化工作")
    }

    override fun getWrappedClass(): Class<*> {
        return getWrappedInstance()::class.java
    }

    override fun setPropertyValue(name: String, value: Any?) {
        // 如果找不到字段直接return
        val field = getField(name) ?: return
        // 确保字段可用，并去进行set
        ReflectionUtils.makeAccessiable(field)
        ReflectionUtils.setField(field, getWrappedInstance(), value)
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

    override fun setPropertyValue(propertyValue: PropertyValue) {
        setPropertyValue(propertyValue.name, propertyValue.value)
    }

    override fun getPropertyValue(name: String): Any? {
        // 如果找不到字段那么直接return null
        val field = getField(name) ?: return null
        ReflectionUtils.makeAccessiable(field)
        return ReflectionUtils.getField(field, getWrappedInstance())
    }

    override fun getPropertyType(name: String): Class<*>? {
        val propertyValue = getPropertyValue(name)
        if (propertyValue != null) {
            return propertyValue::class.java
        }
        return null
    }

    override fun setPropertyValues(pvs: PropertyValues) {
        pvs.getPropertyValues().forEach(this::setPropertyValue)
    }

    override fun setPropertyValues(pvs: Map<String, Any?>) {
        pvs.forEach { (k, v) -> setPropertyValue(PropertyValue(k, v)) }
    }
}