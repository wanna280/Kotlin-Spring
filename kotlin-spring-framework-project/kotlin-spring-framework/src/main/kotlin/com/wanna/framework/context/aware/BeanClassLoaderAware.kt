package com.wanna.framework.context.aware

/**
 * 完成BeanClassLoader的注册的Aware
 */
interface BeanClassLoaderAware : Aware {
    fun setBeanClassLoader(classLoader: ClassLoader)
}