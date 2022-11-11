package com.wanna.framework.beans.factory

import com.wanna.framework.lang.Nullable

/**
 * 这是一个支持继承的BeanFactory，在BeanFactory的基础功能上新增了继承的功能
 *
 * @see getParentBeanFactory
 */
interface HierarchicalBeanFactory : BeanFactory {
    /**
     * 获取一个BeanFactory的parentBeanFactory
     *
     * @return 如果有parentBeanFactory的话，return parentBeanFactory；如果没有的话，return null
     */
    @Nullable
    fun getParentBeanFactory() : BeanFactory?
}