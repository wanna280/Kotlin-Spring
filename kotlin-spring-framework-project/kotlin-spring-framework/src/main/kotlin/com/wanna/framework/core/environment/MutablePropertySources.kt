package com.wanna.framework.core.environment

import java.util.concurrent.CopyOnWriteArrayList

/**
 * 这是一个多个PropertySources的聚合，提供了线程安全的访问，对于任何相关的操作都会加上锁去进行操作
 */
open class MutablePropertySources() : PropertySources {
    // PropertySource列表
    private val propertySourceList = CopyOnWriteArrayList<PropertySource<*>>()

    /**
     * 提供一个PropertySources作为参数的构造器，对PropertySource列表去进行初始化
     */
    constructor(propertySources: PropertySources) : this() {
        propertySourceList += propertySources
    }

    override fun contains(name: String): Boolean {
        propertySourceList.forEach {
            if (it.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * 按照name去进行替换PropertySource
     *
     * @param name name
     * @param propertySource 要进行替换的PropertySource
     */
    open fun replace(name: String, propertySource: PropertySource<*>) {
        val index = propertySourceList.indices.filter { propertySourceList[it].name == name }.toList()
        if (index.isNotEmpty()) {
            replace(index[0], propertySource)
        }
    }

    override fun get(name: String): PropertySource<*>? {
        propertySourceList.forEach {
            if (it.name == name) {
                return it
            }
        }
        return null
    }

    override fun iterator(): Iterator<PropertySource<*>> {
        return propertySourceList.iterator()
    }

    private fun removeIfPresent(propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            this.propertySourceList.remove(propertySource)
        }
    }

    open fun replace(index: Int, propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            this.propertySourceList[index] = propertySource
        }
    }

    /**
     * 将PropertySource插入到列表的头部
     */
    open fun addFirst(propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            removeIfPresent(propertySource)
            this.propertySourceList.add(0, propertySource)
        }
    }

    /**
     * 将指定的PropertySource插入到列表的尾部
     */
    open fun addLast(propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            removeIfPresent(propertySource)
            this.propertySourceList.add(propertySource)
        }
    }

    /**
     * 将指定的PropertySource插入到指定的index的位置
     *
     * @param index 要插入的位置的index
     * @param propertySource 要插入的PropertySource
     */
    open fun addAtIndex(index: Int, propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            removeIfPresent(propertySource)
            this.propertySourceList.add(index, propertySource)
        }
    }

    /**
     * 将指定的PropertySource加入到指定的propertyName之前
     *
     * @param beforeName 指定的propertyName
     * @param propertySource 要插入的PropertySource
     */
    open fun addBefore(beforeName: String, propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            val beforeIndex = getIndex(beforeName)
            if (beforeIndex == -1) {
                throw IllegalStateException("Cannot get property source by name $beforeName")
            }
            addAtIndex(beforeIndex, propertySource)
        }
    }

    /**
     * 将指定的PropertySource加入到指定的propertyName之后
     *
     * @param afterName 指定的propertyName
     * @param propertySource 要插入的PropertySource
     */
    open fun addAfter(afterName: String, propertySource: PropertySource<*>) {
        synchronized(this.propertySourceList) {
            val afterIndex = getIndex(afterName)
            if (afterIndex == -1) {
                throw IllegalStateException("Cannot get property source by name $afterName")
            }
            addAtIndex(afterIndex + 1, propertySource)
        }
    }

    /**
     * 根据name去移除列表当中的PropertySource
     */
    open fun remove(name: String): PropertySource<*>? {
        synchronized(this.propertySourceList) {
            val index = getIndex(name)
            if (index == -1) {
                return null
            }
            val propertySource = propertySourceList[index]
            removeIfPresent(propertySource)
            return propertySource
        }
    }

    private fun getIndex(name: String): Int {
        for (index in 0 until propertySourceList.size) {
            if (propertySourceList[index].name == name) {
                return index
            }
        }
        return -1
    }
}