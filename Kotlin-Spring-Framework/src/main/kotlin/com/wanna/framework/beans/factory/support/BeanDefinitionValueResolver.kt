package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.PropertyValue
import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.config.RuntimeBeanReference
import com.wanna.framework.beans.factory.config.TypedStringValue
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * BeanDefinition的值解析器
 *
 * @param beanDefinition beanDefinition
 * @param beanName beanName
 * @param beanFactory beanFactory
 * @param typeConverter typeConverter
 */
class BeanDefinitionValueResolver(
    private val beanFactory: AbstractAutowireCapableBeanFactory,
    private val beanName: String,
    private val beanDefinition: BeanDefinition,
    private val typeConverter: TypeConverter
) {
    /**
     * 如果必要的话，需要去解析BeanDefinition当中的属性值
     *
     * 支持各种各样的类型，比如：
     * * 1.RuntimeBeanReference
     * * 2.ManagedList(ArrayList<RuntimeBeanReference>)
     * * 3.TypedValueString
     *
     * @param pv propertyValue
     */
    @Suppress("UNCHECKED_CAST")
    fun resolveValueIfNecessary(pv: PropertyValue, originValue: Any?): Any? {
        val pvValue = pv.value
        if (pvValue is RuntimeBeanReference) {
            return beanFactory.getBean(pvValue.getBeanName())
        } else if (pvValue is ManagedList<*>) {
            val managedList = pvValue as ManagedList<RuntimeBeanReference>
            return managedList.map {
                resolveValueIfNecessary(PropertyValue(pv.name, it), originValue)
            }.toList()
        } else if (pvValue is NullBean) {
            return null
        } else if (pvValue is TypedStringValue) {
            val resolvedValue = this.beanFactory.resolveEmbeddedValue(pvValue.value)
            val targetType = resolveTargetType(pvValue)
            return if (targetType != null) {
                typeConverter.convertIfNecessary(resolvedValue, targetType)
            } else {
                targetType
            }
        }
        return pvValue
    }


    /**
     * 从TypedStringValue当中去解析到目标类型
     *
     * @param value Typed String Value
     */
    fun resolveTargetType(value: TypedStringValue): Class<*>? {
        return value.resolveTargetType(this.beanFactory.getBeanClassLoader())
    }
}