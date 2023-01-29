package com.wanna.framework.beans

import java.beans.PropertyDescriptor
import kotlin.jvm.Throws

/**
 * 这是一个BeanWrapper, 提供对于一个Bean的包装, 提供了对属性值的访问的相关功能
 *
 * @see ConfigurablePropertyAccessor
 */
interface BeanWrapper : ConfigurablePropertyAccessor {

    /**
     * 获取到包装的对象实例
     *
     * @return 包装的对象实例
     */
    fun getWrappedInstance(): Any

    /**
     * 获取包装的对象类型
     *
     * @return 包装的对象类型
     */
    fun getWrappedClass(): Class<*>

    /**
     * 获取到beanClass当中的所有属性列表
     *
     * @return wrappedClass当中的属性值列表
     */
    fun getPropertyDescriptors(): Array<PropertyDescriptor>

    /**
     * 根据属性名, 去获取到beanClass当中的对应的属性
     *
     * @param propertyName 要去进行获取的属性的属性名
     * @return wrappedClass当中的给定的propertyName的属性值
     * @throws InvalidPropertyException 如果给定的属性名的[PropertyDescriptor]无法获取到的话
     */
    @Throws(InvalidPropertyException::class)
    fun getPropertyDescriptor(propertyName: String): PropertyDescriptor
}