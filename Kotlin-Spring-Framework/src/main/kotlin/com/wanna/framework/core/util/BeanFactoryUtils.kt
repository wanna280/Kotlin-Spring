package com.wanna.framework.core.util

import com.wanna.framework.beans.factory.BeanFactory.Companion.FACTORY_BEAN_PREFIX
import com.wanna.framework.beans.factory.HierarchicalBeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory

/**
 * 这是一个BeanFactory的工具类，提供对BeanFactory当中相关操作提供公共方法
 */
object BeanFactoryUtils {
    /**
     * 判断beanName是否是以FactoryBean的前缀(&)作为开始
     */
    @JvmStatic
    fun isFactoryDereference(beanName: String): Boolean {
        return beanName.startsWith(FACTORY_BEAN_PREFIX)
    }

    /**
     * 将beanName前缀中的"&"全部去掉，实现解引用
     */
    @JvmStatic
    fun transformBeanName(beanName: String): String {
        if (!beanName.startsWith(FACTORY_BEAN_PREFIX)) {
            return beanName
        }
        // 一个for循环切掉所有的FactoryBean的prefix，也就是"&"
        var name = beanName
        do {
            name = name.substring(FACTORY_BEAN_PREFIX.length)
        } while (beanName.startsWith(FACTORY_BEAN_PREFIX))
        return name
    }

    /**
     * 从beanFactory当中去获取符合指定类型的BeanName列表(包含parentBeanFactory)
     *
     * @param lbf 目标BeanFactory
     * @param type 要去BeanFactory当中去进行获取的指定类型
     * @return 该类型的所有BeanNames
     */
    @JvmStatic
    fun beanNamesForTypeIncludingAncestors(lbf: ListableBeanFactory, type: Class<*>): Array<String> {
        // 先获取当前BeanFactory当中的
        val result = ArrayList(lbf.getBeanNamesForType(type))
        if (lbf is HierarchicalBeanFactory) {
            val parentBeanFactory = lbf.getParentBeanFactory()
            if (parentBeanFactory is ListableBeanFactory) {
                result += beanNamesForTypeIncludingAncestors(parentBeanFactory, type)  // 递归
            }
        }
        return result.toTypedArray()
    }

    /**
     * 从beanFactory当中去获取符合指定类型的BeanName列表(不包含parentBeanFactory)
     */
    @JvmStatic
    fun beanNamesOfType(lbf: ListableBeanFactory, type: Class<*>): Array<String> {
        val result = ArrayList(lbf.getBeanNamesForType(type))
        return result.toTypedArray()
    }
}