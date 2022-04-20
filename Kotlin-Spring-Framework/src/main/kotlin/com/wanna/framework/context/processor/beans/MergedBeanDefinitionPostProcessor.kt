package com.wanna.framework.context.processor.beans

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition

interface MergedBeanDefinitionPostProcessor : BeanPostProcessor {

    /**
     * 对MergedBeanDefinition的后置处理工作，在Spring当中，因为BeanDefinition会存在有parent BeanDefinition
     * 因此在Bean的实例化之后、完成属性的赋值之前，应该给BeanPostProcessor一个机会，让它去对parent BeanDefinition中的相关属性
     * 合并到当前的BeanDefinition当中来
     */
    fun postProcessMergedBeanDefinition(beanDefinition: RootBeanDefinition, beanType: Class<*>, beanName: String) {}


}