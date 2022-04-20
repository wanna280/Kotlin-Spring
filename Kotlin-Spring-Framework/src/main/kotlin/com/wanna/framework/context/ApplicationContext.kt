package com.wanna.framework.context

import com.wanna.framework.core.environment.Environment

/**
 * 这是一个Spring应用的上下文，它继承了BeanFactory，并拥有了BeanFactory中的所有功能
 */
interface ApplicationContext : BeanFactory {

    /**
     * 获取可以自动装配的BeanFactory，可以用它完成Bean的创建/销毁等工作
     */
    fun getAutowireCapableBeanFactory(): AutowireCapableBeanFactory

    /**
     * 获取ApplicationContext的Environment信息
     */
    fun getEnvironment(): Environment
}