package com.wanna.framework.context.annotation

import com.wanna.framework.beans.BeanFactoryAware

/**
 * 配置类的增强器
 */
class ConfigurationClassEnhancer {
    fun enhance(beanClass: Class<*>, classLoader: ClassLoader?): Class<*> {
        return beanClass
    }

    interface EnhancedConfiguration : BeanFactoryAware
}