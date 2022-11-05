package com.wanna.framework.context

/**
 * 这是一个ApplicationContext的Initializer，可以对ApplicationContext去完成自定义的初始化工作
 */
@FunctionalInterface
interface ApplicationContextInitializer<T : ConfigurableApplicationContext> {
    fun initialize(applicationContext: T)
}