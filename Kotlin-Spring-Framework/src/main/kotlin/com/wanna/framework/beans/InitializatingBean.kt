package com.wanna.framework.beans

/**
 * Bean的初始化方法
 */
interface InitializatingBean {
    fun afterPropertiesSet();
}