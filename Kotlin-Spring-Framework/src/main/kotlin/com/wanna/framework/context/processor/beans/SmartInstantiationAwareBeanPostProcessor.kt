package com.wanna.framework.context.processor.beans

import java.lang.reflect.Constructor

interface SmartInstantiationAwareBeanPostProcessor : InstantiationAwareBeanPostProcessor {

    /**
     * 根据beanClass和beanName，去预测beanType，返回的是这个processor在处理实例化之前的回调时，是否已经缓存过bean的真实类型，
     * 比如AbstractAutoProxyCreator中就会缓存proxyType，也就是该Bean完成代理之后的类型，它的主要作用是方便BeanFactory去进行类型的推断和获取，
     * 比如用于isTypeMatch去对Bean的类型去进行推断
     *
     * @see com.wanna.framework.aop.creator.AbstractAutoProxyCreator
     * @return 如果缓存过，return缓存过的类型；如果没有缓存过，那么return null
     */
    fun predictBeanType(beanClass: Class<*>, beanName: String): Class<*>? {
        return null
    }

    /**
     * 获取Bean的早期引用
     */
    fun getEarlyReference(bean: Any, beanName: String): Any {
        return bean
    }

    /**
     * 推断出合适的构造器，作为最终创建对象的候选构造器
     *
     * @param beanName beanName
     * @param beanClass beanClass
     * @return 如果推断出来合适的构造器，那么返回推断出来的构造器列表；如果没有推断出来合适的构造器，那么return null
     */
    fun determineCandidateConstructors(beanClass: Class<*>, beanName: String): Array<Constructor<*>>? {
        return null
    }
}