package com.wanna.framework.aop.framework.autoproxy

import com.wanna.framework.aop.TargetSource

/**
 * 这是一个TargetSource的Creator，主要根据beanName和beanClass去匹配该Bean是否需要有TargetSource？
 * 如果该Bean确实需要有TargetSource，那么需要使用AOP去完成代理，为TargetSource提供支持
 *
 * @see TargetSource
 */
interface TargetSourceCreator {

    /**
     * 如果对当前的Bean感兴趣的话，为该Bean，去利用TargetSource去提供Bean的来源
     */
    fun getTargetSource(beanClass: Class<*>, beanNameString: String): TargetSource?
}