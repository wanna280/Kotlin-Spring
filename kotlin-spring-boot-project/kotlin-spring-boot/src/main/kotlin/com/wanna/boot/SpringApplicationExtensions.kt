package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * Note:Kotlin当中的inline方法, 在泛型上加了**reified**关键字, 是那么在该inline的方法当中是可以使用到泛型的类型的; 
 * 因为Kotlin的编译器它在编译时, 它知道你的泛型类型呀！编译器在编译时直接帮你把类型给替换了不就完了, 因此才可以实现下面的扩展函数
 */

/**
 * Spring为Kotlin提供的扩展函数, 支持更加快捷的方式去运行SpringApplication
 *
 * @param T 主启动类
 * @param args 命令行参数
 * @return SpringApplication构建完成的最终ApplicationContext
 */
inline fun <reified T : Any> runSpringApplication(vararg args: String): ConfigurableApplicationContext =
    SpringApplication.run(T::class.java, *args)


/**
 * Spring为Kotlin提供的扩展函数, 支持在启动SpringApplication之前, 去对SpringApplication去进行更多自定义的配置
 *
 * @param args 命令行参数
 * @param T 主启动类
 * @param init 启动SpringApplication之前的初始化函数(使用Receiver的方式, 可以在lambda表达式当中将this传递进去, this的类型是"()"之前的类型)
 * @return SpringApplication构建完成的最终ApplicationContext
 */
inline fun <reified T : Any> runSpringApplication(
    vararg args: String, init: SpringApplication.() -> Unit
): ConfigurableApplicationContext = SpringApplication(T::class.java).apply(init).run(*args)