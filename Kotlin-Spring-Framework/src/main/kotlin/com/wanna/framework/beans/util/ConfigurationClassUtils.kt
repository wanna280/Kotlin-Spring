package com.wanna.framework.beans.util

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 这是一个ConfigurationClass的配置类，在ConfigurationClassPostProcessor扫描时，会将一个类包装成为一个ConfigurationClass(配置类)，
 * 这是一个提供ConfigurationClass的相关工具类
 */
class ConfigurationClassUtils {
    companion object {

        // 配置类的属性
        const val CONFIGURATION_CLASS_ATTRIBUTE = "configurationClass"
        const val CONFIGURATION_CLASS_FULL = "full"  // full，全配置类(@Configuration)
        const val CONFIGURATION_CLASS_LITE = "lite"  // lite，半配置类

        /**
         * 检查一个ConfigurationClass是否是候选的？
         */
        @JvmStatic
        fun checkConfigurationClassCandidate(beanDefinition: BeanDefinition): Boolean {
            var beanClass = beanDefinition.getBeanClass()
            return true
        }
    }
}