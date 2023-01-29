package com.wanna.framework.beans.factory

/**
 * 完成BeanClassLoader的自动注入的Aware
 */
fun interface BeanClassLoaderAware : Aware {

    /**
     * 自动注入BeanClassLoader
     *
     * @param classLoader beanClassLoader
     */
    fun setBeanClassLoader(classLoader: ClassLoader)
}