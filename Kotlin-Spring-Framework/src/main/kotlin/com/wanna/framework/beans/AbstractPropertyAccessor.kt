package com.wanna.framework.beans

/**
 * 这是一个抽象的属性访问器，它组合了TypeConverter和PropertyAccessor，可以去进行属性的设置工作；
 * 对进行设置的属性值值，可以去支持类型的转换，比如设置一个Int的属性，但是该字段支持使用String类型，字段并不一定需要Int类型；
 * 原因在于它属性访问器本身组合了TypeConverter(整合ConversionService和PropertyEditor)，它能够去支持类型的自动转换
 */
abstract class AbstractPropertyAccessor : TypeConverterSupport(), ConfigurablePropertyAccessor {
    init {
        this.delegate = TypeConverterDelegate(this)
    }
}