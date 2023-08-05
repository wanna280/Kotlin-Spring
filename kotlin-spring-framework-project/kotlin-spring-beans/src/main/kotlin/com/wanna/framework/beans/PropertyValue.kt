package com.wanna.framework.beans

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.lang.Nullable

/**
 * 描述的是一个对象的属性值
 *
 * @param name propertyName
 * @param value propertyValue
 */
open class PropertyValue(val name: String, @Nullable var value: Any?) :
    BeanMetadataAttributeAccessor() {

    /**
     * 该PropertyValue是否是可选的?
     */
    var optional: Boolean = true

    /**
     * 已经解析完成的Tokens
     */
    @Nullable
    var resolvedTokens: Any? = null

    /**
     * 已经完成类型转换之后的值
     */
    @Nullable
    private var convertedValue: Any? = null

    /**
     * 当前的属性值是否已经完成了类型转换?
     */
    private var converted: Boolean = false

    /**
     * 设置已经完成类型转换之后的值
     *
     * @param value value
     */
    @Synchronized
    open fun setConvertedValue(@Nullable value: Any?) {
        this.convertedValue = value
        this.converted = true
    }

    /**
     * 获取已经完成类型转换之后的属性值
     *
     * @return 完成类型转换之后的值, 如果不存在的话, return null
     */
    @Nullable
    @Synchronized
    open fun getConvertedValue(): Any? = this.convertedValue


    /**
     * 检查当前属性值, 是否已经经过了类型转换?
     *
     * @return 如果已经经过了类型转换, 那么return true; 否则return false
     */
    @Synchronized
    open fun isConverted(): Boolean = this.converted

    override fun toString() = "PropertyValue(name='$name', value=$value)"
}