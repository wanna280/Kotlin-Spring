package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder

/**
 * 这是一个BeanDefinition的实现
 * @see BeanDefinition
 */
open class RootBeanDefinition constructor(beanClass: Class<*>?) :
    AbstractBeanDefinition(beanClass) {

    // 进行后置处理的锁
    var postProcessLock = Any()

    // 是否已经被merged？
    var postProcessed: Boolean = false

    /**
     * 调用super.copy方法将元素拷贝到当前对象当中
     */
    constructor(beanDefinition: BeanDefinition) : this(null) {
        super.copy(beanDefinition, this)
    }

    // 一个BeanDefinition所装饰的BeanDefinition
    var decoratedDefinition: BeanDefinitionHolder? = null

    // 是否是FactoryBean
    private var isFactoryBean = false

    open fun isFactoryBean(): Boolean {
        return isFactoryBean
    }

    open fun setFactoryBean(isFactoryBean: Boolean) {
        this.isFactoryBean = isFactoryBean
    }
}