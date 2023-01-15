package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement

/**
 * Bean的引用, 暂时设置为beanName, 后期支持去进行自动解析
 *
 * @see RuntimeBeanReference
 */
interface BeanReference : BeanMetadataElement {
    fun getBeanName(): String
}