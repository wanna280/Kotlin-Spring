package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * ScopeMetadata的解析器, 负责解析一个BeanDefinition当中的类型的Scope
 */
fun interface ScopeMetadataResolver {

    /**
     * 根据BeanDefinition去解析出来对应的Scope
     *
     * @param definition BeanDefinition
     * @return 解析得到的Scope元信息
     */
    fun resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata
}