package com.wanna.framework.core.environment

/**
 * 这是一个聚合的PropertySource, 内部聚合了多个PropertySource
 *
 * @see PropertySource
 * @see EnumerablePropertySource
 */
open class CompositePropertySource(name: String) : EnumerablePropertySource<Any>(name) {

    // 聚合的多个PropertySource
    private val propertySources = LinkedHashSet<PropertySource<*>>()

    /**
     * 如何获取一个指定的属性值? 从PropertySource列表当中, 挨个去进行检查;
     * (1)如果其中一个PropertySource当中获取到了属性值的话, 那么就return;
     * (2)如果在所有的PropertySource当中都没有找到的话, 那么return null
     */
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
     * 遍历所有的PropertySource, 如果其中一个PropertySource当中包含了该属性值, 那么return true;
     * 如果所有的PropertySource当中都没有包含该属性值的话, 那么return false
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
     * 将PropertySource加入到整个链的首部
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
     * 将PropertySource加入到整个链的尾部
     *
     * @param propertySource 需要添加的PropertySource
     */
    open fun addPropertySource(propertySource: PropertySource<*>) {
        this.propertySources += propertySource
    }

    /**
     * 获取所有的PropertySource列表
     */
    open fun getPropertySources(): MutableCollection<PropertySource<*>> = this.propertySources

    /**
     * 获取PropertySources列表当中的所有的propertyName列表, 只有类型是EnumerablePropertySource的才能参与统计
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