package com.wanna.framework.context.annotations

/**
 * BeanDefinitionReader
 */
interface BeanDefinitionReader {

    fun loadBeanDefinitions(location:String) : Int
}