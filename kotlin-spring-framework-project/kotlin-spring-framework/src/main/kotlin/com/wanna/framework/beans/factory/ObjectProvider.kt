package com.wanna.framework.beans.factory

/**
 * ObjectFactory的变体, 主要用于在InjectPoint(注入点)当中去完成依赖的注入时去进行使用
 *
 * @see ObjectFactory
 */
interface ObjectProvider<T : Any> : ObjectFactory<T> {
    /**
     * 如果存在有Bean的话, 才进行获取; 如果获取不到的话, return null
     *
     * @return 如果存在, return Bean; 否则return null
     */
    fun getIfAvailable(): T?
}