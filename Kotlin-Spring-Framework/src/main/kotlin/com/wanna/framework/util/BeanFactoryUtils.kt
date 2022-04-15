package com.wanna.framework.util

import com.wanna.framework.context.BeanFactory

class BeanFactoryUtils {
    companion object {

        /**
         * 判断beanName是否是以FactoryBean的前缀(&)作为开始
         */
        @JvmStatic
        fun isFactoryDereference(beanName: String): Boolean {
            return beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)
        }
    }
}