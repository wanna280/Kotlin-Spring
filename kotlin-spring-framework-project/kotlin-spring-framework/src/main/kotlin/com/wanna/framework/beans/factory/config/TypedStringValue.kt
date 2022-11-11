package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement
import com.wanna.framework.util.ClassUtils
import kotlin.jvm.Throws

/**
 * TypedStringValue，支持添加到BeanDefinition的PropertyValues当中在运行时去进行占位符解析
 *
 * @see BeanDefinition.getPropertyValues
 * @see MutablePropertyValues.addPropertyValue
 *
 * @param value 要去进行解析的字符串表达式
 * @param targetType targetType or targetTypeName(需要把value解析成为什么类型？支持使用TypeConverter去完成转换)
 */
open class TypedStringValue(val value: String? = null, val targetType: Any? = null) : BeanMetadataElement {

    // 提供一个参数的构造器
    constructor(value: String?) : this(value, null)

    private var source: Any? = null

    override fun getSource() = this.source

    open fun setSource(source: Any?) {
        this.source = source
    }

    /**
     * 判断是否设置了targetType(Class)
     *
     * @return 如果设置了targetType，那么return true；否则return false
     */
    open fun hasTargetType(): Boolean = targetType is Class<*>

    /**
     * 获取targetType
     *
     * @return 获取targetType
     * @throws IllegalStateException 如果targetType没有被设置，获取被设置的不是Class类型
     */
    @Throws(IllegalStateException::class)
    open fun getTargetType(): Class<*>? {
        if (targetType is Class<*>) {
            return targetType
        }
        throw IllegalStateException("Typed String Value没有设置一个可以解析的targetType")
    }

    /**
     * 解析targetType(如果targetType被设置为String类型的话)
     *
     * @param classLoader 要去进行解析目标类的ClassLoader
     * @return 解析到的目标类的类型(如果没有设置，那么return null)
     */
    open fun resolveTargetType(classLoader: ClassLoader): Class<*>? {
        val targetTypeName = getTargetTypeName()
        targetTypeName ?: return null
        return ClassUtils.forName<Any>(targetTypeName, classLoader)
    }

    /**
     * 获取targetType的name，如果是String，直接return；如果是Class，那么使用className
     *
     * @return targetType的className
     */
    protected open fun getTargetTypeName(): String? {
        return if (targetType is String) targetType else (targetType as Class<*>?)?.name
    }
}