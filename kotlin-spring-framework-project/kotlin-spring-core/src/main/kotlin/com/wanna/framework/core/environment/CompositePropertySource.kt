package com.wanna.framework.core.environment

import com.wanna.framework.lang.Nullable

/**
 * 这是一个聚合的[PropertySource], 内部聚合了多个[PropertySource]去对外提供访问
 *
 * @see PropertySource
 * @see EnumerablePropertySource
 *
 * @param name PropertySource Name
 */
open class CompositePropertySource(name: String) : EnumerablePropertySource<Any>(name) {

    /**
     * 聚合的多个[PropertySource]的列表
     */
    private val propertySources = LinkedHashSet<PropertySource<*>>()

    /**
     * 如何获取一个指定的属性值? 从[PropertySource]列表当中, 挨个去进行检查;
     * (1)如果其中一个[PropertySource]当中获取到了属性值的话, 那么就return;
     * (2)如果在所有的[PropertySource]当中都没有找到的话, 那么return null
     *
     * @param name 要去获取属性值的属性名
     * @return 获取到的属性值
     */
    @Nullable
    override fun getProperty(name: String): Any? {
        propertySources.forEach {
            val property = it.getProperty(name)
            if (property != null) {
                return property
            }
        }
        return null
    }

    /**
     * 遍历所有的[PropertySource], 如果其中一个[PropertySource]当中包含了该属性值, 那么return true;
     * 如果所有的[PropertySource]当中都没有包含该属性值的话, 那么return false.
     *
     * @param name 要去检查的属性名
     * @return 如果其中一个PropertySource当中有这样的属性名, return true; 如果都不存在的话return false
     */
    override fun containsProperty(name: String): Boolean {
        propertySources.forEach {
            if (it.containsProperty(name)) {
                return true
            }
        }
        return false
    }

    /**
     * 将给定的[PropertySource]加入到整个[PropertySource]列表的首部
     *
     * @param propertySource 需要添加的PropertySource
     */
    open fun addFirstPropertySource(propertySource: PropertySource<*>) {
        val ps = ArrayList(propertySources)
        propertySources.clear()
        propertySources += propertySource
        propertySources += ps
    }

    /**
     * 将给定的[PropertySource]去加入到整个链的尾部
     *
     * @param propertySource 需要添加的PropertySource
     */
    open fun addPropertySource(propertySource: PropertySource<*>) {
        this.propertySources += propertySource
    }

    /**
     * 获取所有的[PropertySource]列表
     *
     * @return PropertySource列表
     */
    open fun getPropertySources(): MutableCollection<PropertySource<*>> = this.propertySources

    /**
     * 统计当前[CompositePropertySource]当前组合的全部的[PropertySource]列表当中的所有的propertyName列表
     *
     * Note: 只有类型是[EnumerablePropertySource]的才能参与统计
     *
     * @see EnumerablePropertySource
     */
    override fun getPropertyNames(): Array<String> {
        val propertyNames = LinkedHashSet<String>()
        propertySources.forEach {
            if (it is EnumerablePropertySource<*>) {
                propertyNames += it.getPropertyNames()
            }
        }
        return propertyNames.toTypedArray()
    }
}