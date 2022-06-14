package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.PropertyValue
import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.config.RuntimeBeanReference
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
     *
     * @param pv propertyValue
     */
    fun resolveValueIfNecessary(pv: PropertyValue, originValue: Any?): Any? {
        if (pv.value is RuntimeBeanReference) {
            return beanFactory.getBean((pv.value as RuntimeBeanReference).getBeanName())
        }
        return pv.value
    }
}