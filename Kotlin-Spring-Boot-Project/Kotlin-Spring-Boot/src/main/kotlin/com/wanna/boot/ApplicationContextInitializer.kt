package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个ApplicationContext的Initializer，可以对ApplicationContext去完成自定义的初始化工作
 *
 * @see SpringApplication.run
 */
@FunctionalInterface
interface ApplicationContextInitializer<T : ConfigurableApplicationContext> {
    fun initialize(applicationContext: T)
}