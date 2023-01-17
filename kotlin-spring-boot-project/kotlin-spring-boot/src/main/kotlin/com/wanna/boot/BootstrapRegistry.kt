package com.wanna.boot

import com.wanna.framework.context.event.ApplicationListener
import java.util.function.Supplier

/**
 * 这是一个Bootstrap的注册中心, 提供对于实例的注册功能
 *
 * @see BootstrapContext
 * @see ConfigurableBootstrapContext
 */
interface BootstrapRegistry {

    /**
     * 注册一个具体类型的实例对象到BootstrapRegistry当中
     *
     * @param type type
     * @param instanceSupplier 创建对象的InstanceSupplier
     */
    fun <T> register(type: Class<T>, instanceSupplier: InstanceSupplier<T>)

    /**
     * 如果之前没有该类型的对象的话, 那么去注册一个到BootstrapRegistry当中; 如果之前已经有的话, 那么就不必注册
     *
     * @param type type
     * @param instanceSupplier 创建对象的InstanceSupplier
     */
    fun <T> registerIfAbsent(type: Class<T>, instanceSupplier: InstanceSupplier<T>)

    /**
     * 判断BootstrapRegistry当中是否已经注册过该类型的实例?
     *
     * @param type type
     * @return 如果已经注册过, return true; 否则return false
     */
    fun <T> isRegistered(type: Class<T>): Boolean

    /**
     * 根据类型去获取它所注册的InstanceSupplier
     *
     * @param type type
     * @return 该类型对于的InstanceSupplier(如果不存在该类型对应的实例, 那么return null)
     */
    fun <T> getRegisteredInstanceSupplier(type: Class<T>): InstanceSupplier<T>?

    /**
     * 添加一个处理BootstrapContext关闭的事件的监听器
     *
     * @param listener 想要注册的ApplicationListener
     */
    fun addCloseListener(listener: ApplicationListener<BootstrapContextClosedEvent>)


    /**
     * InstanceSupplier, 提供对象的创建
     */
    interface InstanceSupplier<T> {

        /**
         * 提供实例的创建的Callback方法
         *
         * @param context BootstrapContext
         * @return 创建出来的实例
         */
        fun get(context: BootstrapContext): T

        /**
         * 实例对象的作用域, 单例/原型
         *
         * @return 实例对象的作用域
         */
        fun getScope(): Scope = Scope.SINGLETON

        companion object {

            /**
             * 给定一个实例去构建出来InstanceSupplier对象的工厂方法
             *
             * @param instance 实例对象
             * @return 返回创建好的InstanceSupplier
             */
            @JvmStatic
            fun <T> of(instance: T): InstanceSupplier<T> = object : InstanceSupplier<T> {
                override fun get(context: BootstrapContext): T = instance
            }

            /**
             * 给定一个Supplier去构建出来InstanceSupplier对象的工厂方法
             *
             * @param supplier 创建目标对象的Supplier
             * @return 返回创建好的InstanceSupplier
             */
            @JvmStatic
            fun <T> of(supplier: Supplier<T>): InstanceSupplier<T> = object : InstanceSupplier<T> {
                override fun get(context: BootstrapContext): T = supplier.get()
            }
        }
    }

    enum class Scope {
        SINGLETON, PROTOTYPE
    }
}