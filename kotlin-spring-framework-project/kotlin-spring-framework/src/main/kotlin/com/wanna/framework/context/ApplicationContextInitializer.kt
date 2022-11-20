package com.wanna.framework.context

/**
 * [ApplicationContext]的Initializer初始化器，可以对[ApplicationContext]去完成自定义的初始化工作
 *
 * @param T ApplicationContext的类型, 只有[ConfigurableApplicationContext]才支持去进行自定义
 */
@FunctionalInterface
fun interface ApplicationContextInitializer<T : ConfigurableApplicationContext> {

    /**
     * 将[ApplicationContext]传递给你，去对[ApplicationContext]去进行更多的自定义
     *
     * @param applicationContext ApplicationContext
     */
    fun initialize(applicationContext: T)
}