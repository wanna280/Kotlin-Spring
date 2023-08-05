package com.wanna.framework.beans.factory.config

/**
 * 这是一个BeanFactoryPostProcessor, 可以在BeanFactory初始化过程中, 对BeanFactory去进进行干预工作;
 * 它有一个子接口[BeanDefinitionRegistryPostProcessor]比较常用, 用于去提供BeanDefinition的注册,
 * 也就是提供将自定义的BeanDefinition去注册到Spring的BeanFactory的功能
 *
 * @see ConfigurableListableBeanFactory
 * @see BeanDefinitionRegistryPostProcessor
 */
interface BeanFactoryPostProcessor {

    /**
     * 实现这个方法, 可以对BeanFactory去完成初始化
     *
     * @param beanFactory 要去进行自定义的BeanFactory
     */
    fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory);
}