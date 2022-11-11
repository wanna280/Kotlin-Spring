package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.util.BeanFactoryUtils

/**
 * 这是一个BeanDefinitionHolder，里面包装了BeanDefinition和beanName
 *
 * @param beanDefinition beanDefinition
 * @param beanName beanName
 */
open class BeanDefinitionHolder(val beanDefinition: BeanDefinition, val beanName: String) :
    BeanMetadataAttributeAccessor() {

    /**
     * 判断name和beanName对应的类型是否匹配？
     *
     * @param name 要比较的候选name
     */
    open fun matchesName(name: String?): Boolean {
        return name != null && (name == beanName || BeanFactoryUtils.transformBeanName(beanName) == name)
    }

}