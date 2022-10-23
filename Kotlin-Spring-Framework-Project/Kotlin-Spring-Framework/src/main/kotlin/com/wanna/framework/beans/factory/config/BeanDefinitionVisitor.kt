package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.util.StringValueResolver

/**
 * 提供对于BeanDefinition的占位符解析的功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
open class BeanDefinitionVisitor(private val valueResolver: StringValueResolver) {

    fun visitBeanDefinition(beanDefinition: BeanDefinition) {

    }
}