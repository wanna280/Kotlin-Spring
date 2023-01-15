package com.wanna.framework.beans

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.lang.Nullable

/**
 * 标识这是一个属性的访问器, 可以支持基于属性的名字对属性的设置以及获取,
 * 也是提供对于[BeanWrapper]的基础类的实现
 *
 * @see BeanWrapperImpl
 * @see BeanWrapper
 * @see PropertyAccessorFactory.forBeanPropertyAccess
 */
interface PropertyAccessor {

    companion object {
        /**
         * 嵌套的属性的分隔符, 例如对于`getFoo().getBar()`, 将会被表述成为"foo.bar"
         */
        const val NESTED_PROPERTY_SEPARATOR = "."

        /**
         * 嵌套的属性的分隔符(Char), 例如对于`getFoo().getBar()`, 将会被表述成为"foo.bar"
         */
        const val NESTED_PROPERTY_SEPARATOR_CHAR = '.'

        /**
         * 属性Key的的前缀, 例如对于"person.address[0]"
         */
        const val PROPERTY_KEY_PREFIX = "["

        /**
         * 属性Key的的前缀(Char), 例如对于"person.address[0]"
         */
        const val PROPERTY_KEY_PREFIX_CHAR = '['

        /**
         * 属性Key的的后缀, 例如对于"person.address[0]"
         */
        const val PROPERTY_KEY_SUFFIX = "]"

        /**
         * 属性Key的的后缀(Char), 例如对于"person.address[0]"
         */
        const val PROPERTY_KEY_SUFFIX_CHAR = ']'
    }


    /**
     * 检查给定的属性名对应的属性是否是一个可读的属性
     * Note: 当属性本身就不存在的话, return false
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 如果该属性可读; 不存在的话, return false
     */
    fun isReadableProperty(name: String): Boolean

    /**
     * 检查给定的属性名对应的属性是否是一个可写的属性?
     * Note: 当属性本身就不存在的话, return false
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 如果该属性值可写, return true; 不存在的话, return false
     */
    fun isWritableProperty(name: String): Boolean

    /**
     * 执行对于属性值的设置
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @param value 对于该属性, 需要去进行设置的值
     */
    fun setPropertyValue(name: String, @Nullable value: Any?)

    /**
     * 执行对于属性值的设置
     *
     * @param propertyValue 属性名&属性值的组合
     */
    fun setPropertyValue(propertyValue: PropertyValue)

    /**
     * 根据属性名去获取到该属性的值
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 获取到的属性值
     */
    @Nullable
    fun getPropertyValue(name: String): Any?

    /**
     * 获取给定的属性名对应的属性的类型
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 获取到的属性对应的类型
     */
    @Nullable
    fun getPropertyType(name: String): Class<*>?

    /**
     * 为指定的属性名对应的属性的类型, 去获取到[TypeDescriptor]
     *
     * @param name 属性名(可能是一个嵌套的属性, 或者是一个indexed/mapped属性, 也就是支持使用'[]'去访问List/Map)
     * @return 获取到的对应的属性对应的TypeDescriptor(获取不到return null)
     */
    @Nullable
    fun getPropertyTypeDescriptor(name: String): TypeDescriptor?

    /**
     * 批量执行属性值的设置
     *
     * @param pvs 要进行设置的属性值列表
     */
    fun setPropertyValues(pvs: PropertyValues)

    /**
     * 批量执行属性值的设置
     *
     * @param pvs 要进行设置的属性值列表
     */
    fun setPropertyValues(pvs: Map<String, Any?>)
}