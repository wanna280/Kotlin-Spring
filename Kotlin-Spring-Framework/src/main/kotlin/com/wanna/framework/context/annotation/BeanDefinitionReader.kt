package com.wanna.framework.context.annotation

/**
 * BeanDefinitionReader
 */
interface BeanDefinitionReader {

    fun loadBeanDefinitions(location:String) : Int
}