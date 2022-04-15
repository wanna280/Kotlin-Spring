package com.wanna.framework.beans.util

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

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