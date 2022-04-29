package com.wanna.framework.util

import com.wanna.framework.context.BeanFactory

/**
 * 这是一个BeanFactory的工具类，提供对BeanFactory当中相关操作提供公共方法
 */
class BeanFactoryUtils {
    companion object {

        /**
         * 判断beanName是否是以FactoryBean的前缀(&)作为开始
         */
        @JvmStatic
        fun isFactoryDereference(beanName: String): Boolean {
            return beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)
        }

        /**
         * 将beanName前缀中的&全部去掉
         */
        @JvmStatic
        fun transformBeanName(beanName: String): String {
            if (!beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
                return beanName
            }
            // 切掉所有的FactoryBean的prefix，也就是&
            var name = beanName
            do {
                name = name.substring(BeanFactory.FACTORY_BEAN_PREFIX.length)
            } while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX))
            return name
        }
    }
}