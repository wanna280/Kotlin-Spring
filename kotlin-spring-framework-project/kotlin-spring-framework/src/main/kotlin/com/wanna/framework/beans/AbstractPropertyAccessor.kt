package com.wanna.framework.beans

import com.wanna.framework.core.convert.ConversionService

/**
 * 这是一个抽象的属性访问器，它组合了TypeConverter和PropertyAccessor，可以去进行属性的设置工作；
 *
 * 对进行设置的属性值值，可以去支持类型的转换，比如设置一个Int的属性，但是该字段支持使用String类型，字段并不一定需要Int类型；
 * 原因在于它属性访问器本身组合了TypeConverter(整合ConversionService和PropertyEditor)，它能够去支持类型的自动转换；
 *
 * 这个类当中，只是提供一些模板实现，对于剩下的该去进行实现的方法，全部都交给子类去进行实现
 */
abstract class AbstractPropertyAccessor : TypeConverterSupport(), ConfigurablePropertyAccessor {

    init {
        this.delegate = TypeConverterDelegate(this)
    }

    private var conversionService: ConversionService? = null

    override var autoGrowNestedPaths: Boolean = false

    /**
     * 设置PropertyValue，对指定的Property的值去进行设置
     *
     * @param propertyValue 要去进行设置的PropertyValue
     */
    override fun setPropertyValue(propertyValue: PropertyValue) {
        setPropertyValue(propertyValue.name, propertyValue.value)
    }


    /**
     * 给定一个propertyName，去获取到该Property对应的属性值类型
     *
     * @param name propertyName
     * @return 该属性值的类型(如果获取不到，return null)
     */
    override fun getPropertyType(name: String): Class<*>? {
        val propertyValue = getPropertyValue(name)
        if (propertyValue != null) {
            return propertyValue::class.java
        }
        return null
    }

    /**
     * 给定一个PropertyValues列表，对所有的属性值去进行属性值的设置
     *
     * @param pvs PropertyValue列表
     */
    override fun setPropertyValues(pvs: PropertyValues) {
        pvs.getPropertyValues().forEach(::setPropertyValue)
    }

    /**
     * 以Map的方式去给定一个PropertyValues列表，对所有的属性值去进行属性值的设置
     *
     * @param pvs PropertyValue列表(Map<K,V>)
     */
    override fun setPropertyValues(pvs: Map<String, Any?>) {
        // wrap to PropertyValues，这样就是共用一套逻辑了
        setPropertyValues(MutablePropertyValues(pvs))
    }

    /**
     * 根据name和value去设置具体的属性值
     *
     * @param name propertyName
     * @param value propertyValue
     */
    abstract override fun setPropertyValue(name: String, value: Any?)

    /**
     * 根据name去获取到Property的Value
     *
     * @param name propertyName
     * @return 根据propertyName去获取到的Property的Value
     */
    abstract override fun getPropertyValue(name: String): Any?
}