package com.wanna.framework.core.environment

/**
 * 这是一个PropertySource，对属性的来源进行的抽象；Spring当中的配置文件、环境变量、系统属性等都会被抽象成为一个PropertySource；
 * 可以被PropertySources去进行聚合成为一个PropertySource列表
 *
 * @see PropertySources
 */
@Suppress("UNCHECKED_CAST")
abstract class PropertySource<T>(var name: String, val source: T) {

    /**
     * 从PropertySource当中去获取属性值，抽象方法，交给子类去实现
     */
    abstract fun getProperty(name: String): Any?

    /**
     * 当前的PropertySource当中是否存在有这样的属性值？
     */
    open fun containsProperty(name: String): Boolean = getProperty(name) != null

    override fun toString(): String  = "name=($name)"
}