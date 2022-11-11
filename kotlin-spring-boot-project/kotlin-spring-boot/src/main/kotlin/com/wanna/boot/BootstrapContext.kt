package com.wanna.boot

import java.util.function.Supplier

/**
 * 在SpringApplication启动过程当中，ApplicationContext的创建已经比较晚了，
 * 但是有可能比较早的需要使用到IOC容器，此时就可以使用BootstrapContext去作为IOC容器；
 *
 * Bootstrap的上下文，提供对于Bootstrap上下文当中的对象获取功能；
 * 对于[ConfigurableBootstrapContext]当中提供了对于[BootstrapContext]的配置功能；
 * 在[DefaultBootstrapContext]当中提供了对于BootstrapContext的默认实现
 *
 * @see ConfigurableBootstrapContext
 * @see DefaultBootstrapContext
 */
interface BootstrapContext {

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例
     *
     * @param type type
     * @param T type
     * @return 根据type类型获取到的实例对象
     * @throws IllegalStateException 如果之前没有注册过该类型的实例
     */
    @Throws(IllegalStateException::class)
    fun <T> get(type: Class<T>): T

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例，如果不存在的话，那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param other 默认的对象
     * @return 根据type类型获取到的实例对象
     */
    fun <T> getOrElse(type: Class<T>, other: T): T

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例，如果不存在的话，那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param other 创建默认对象的Supplier
     * @return 根据type类型获取到的实例对象
     */
    fun <T> getOrElseSupply(type: Class<T>, other: Supplier<T>): T

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例，如果不存在的话，那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param exceptionSupplier 丢出异常的Supplier
     * @return 根据type类型获取到的实例对象
     * @throws IllegalStateException 如果之前没有注册过该类型的实例
     */
    fun <T, X : Throwable> getOrElseThrow(type: Class<T>, exceptionSupplier: Supplier<out X>): T

    /**
     * 判断当前的BootstrapContext当中是否已经注册了给定类型的实例？
     *
     * @param type type
     * @param T Type
     * @return 如果已经注册过，return true；否则return false
     */
    fun <T> isRegistered(type: Class<T>): Boolean
}