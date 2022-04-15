package com.wanna.framework.context

interface ConfigurableBeanFactory : BeanFactory {

    /**
     * 获取BeanFactory的ClassLoader
     */
    fun getBeanClassLoader(): ClassLoader

    /**
     * 设置BeanClassLoader
     * @param classLoader 要设置的ClassLoader，如果为空，将会使用默认的ClassLoader
     */
    fun setBeanClassLoader(classLoader: ClassLoader?)
}