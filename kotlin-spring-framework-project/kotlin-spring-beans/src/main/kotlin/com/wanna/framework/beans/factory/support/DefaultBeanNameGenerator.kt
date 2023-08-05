package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 默认的BeanNameGenerator的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class DefaultBeanNameGenerator : BeanNameGenerator {
    companion object {
        /**
         * 单例对象
         */
        val INSTANCE = DefaultBeanNameGenerator()
    }

    override fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        TODO("Not yet implemented")
    }
}