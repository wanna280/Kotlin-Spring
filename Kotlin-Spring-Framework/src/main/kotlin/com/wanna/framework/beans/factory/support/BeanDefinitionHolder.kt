package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor

/**
 * 这是一个BeanDefinitionHolder，里面包装了BeanDefinition和beanName
 */
class BeanDefinitionHolder(val beanDefinition: BeanDefinition, val beanName: String) : BeanMetadataAttributeAccessor()