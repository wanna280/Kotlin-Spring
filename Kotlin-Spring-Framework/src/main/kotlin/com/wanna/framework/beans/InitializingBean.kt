package com.wanna.framework.beans

/**
 * Bean的初始化方法回调，在对Bean去进行初始化时，需要进行自动回调，完成Bean的初始化工作
 */
interface InitializingBean {
    fun afterPropertiesSet();
}