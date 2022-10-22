package com.wanna.framework.beans.factory.config

/**
 * 这是一个单实例Bean的注册中心，提供单实例Bean的注册/获取，它可以被Spring BeanFactory所实现，去提供单实例Bean的管理；
 * BeanFactory的子类ConfigurableBeanFactory就实现了这个接口当中的方法，去完成Spring BeanFactory当中单实例Bean的管理
 *
 * @see ConfigurableBeanFactory
 * @see com.wanna.framework.beans.factory.support.DefaultSingletonBeanRegistry
 */
interface SingletonBeanRegistry {

    /**
     * 通过beanName，获取单实例Bean，如果不存在这样的Bean，那么return null(支持从二、三级缓存中获取对象)
     *
     * @param beanName beanName
     * @return 根据beanName从注册中心当中去获取到的Bean(获取不到return null)
     */
    fun getSingleton(beanName: String): Any?

    /**
     * 注册一个单实例的Bean到当前的注册中心当中去
     *
     * @param beanName beanName
     * @param singleton 你想要注册的SingletonBean
     * @throws IllegalStateException 如果注册中心中已经有了这样的一个BeanName的Bean
     */
    fun registerSingleton(beanName: String, singleton: Any)

    /**
     * 注册中心中是否存在有这样的Bean，只检查singleObjects，并不检查二三级缓存
     *
     * @param beanName beanName
     * @return 如果当前SingletonBeanRegistry当中存在有这样的beanName的Ban，那么return true；否则return false
     */
    fun containsSingleton(beanName: String): Boolean

    /**
     * 获取单实例Bean的数量，只是返回singleObjects(一级缓存)中的数量，并不检查二三级缓存
     *
     * @return 单例Bean的数量
     */
    fun getSingletonCount(): Int

    /**
     * 获取所有的单实例的BeanName列表，只是返回singleObject的beanName列表，并不检查二三级缓存
     *
     * @return 单例Bean的beanName列表
     */
    fun getSingletonNames(): Array<String>

    /**
     * 获取到要操作单实例Bean的注册中心的锁，需要保证多线程并发情况下的线程安全
     *
     * Note: 凡是要操作单例Bean的缓存时，都可以使用这个锁去进行保证线程安全！
     *
     * @return 操作单例Bean的注册中心的锁
     */
    fun getSingletonMutex(): Any
}