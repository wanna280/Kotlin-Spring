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

    /**
     * 设置属性值，应该使用setter的方式去进行设置
     *
     * @param name name
     * @param value value
     */
    override fun setPropertyValue(name: String, value: Any?) {
        // TODO 这里应该进行更多的类型判断和转换工作

        // add: 采用setter的方式去设置属性值，替换之前的字段设置
        val writeMethodName = "set" + name[0].uppercase() + name.substring(1)
        var isFound = false
        ReflectionUtils.doWithMethods(getWrappedClass()) {
            if (isFound) {
                return@doWithMethods
            }
            val parameterTypes = it.parameterTypes
            if (it.name == writeMethodName && it.parameterCount == 1) {
                ReflectionUtils.makeAccessible(it)
                var targetToInject: Any? = value
                if (value is Collection<*> && !ClassUtils.isAssignFrom(Collection::class.java, parameterTypes[0])) {
                    targetToInject = if (value.isNotEmpty()) value.iterator().next() else null
                }
                ReflectionUtils.invokeMethod(
                    it, getWrappedInstance(), convertIfNecessary(targetToInject, parameterTypes[0])
                )
                isFound = true
            }
        }
    }

    private fun processKeyedProperty(propertyValue: PropertyValue) {

    }

    override fun getPropertyValue(name: String): Any? {
        // add: 采用getter的方式去获取属性值
        val readMethodName = "get" + name[0].uppercase() + name.substring(1)
        var isFound = false
        var returnValue: Any? = null
        ReflectionUtils.doWithMethods(getWrappedClass()) {
            if (isFound) {
                return@doWithMethods
            }
            if (it.name == readMethodName && it.parameterCount == 0) {
                ReflectionUtils.makeAccessible(it)
                returnValue = ReflectionUtils.invokeMethod(it, getWrappedInstance())
                isFound = true
            }
        }
        return returnValue
    }

}