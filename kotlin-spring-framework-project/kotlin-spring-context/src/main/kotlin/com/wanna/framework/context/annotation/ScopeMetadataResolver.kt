package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * [ScopeMetadata]的解析器, 负责将[BeanDefinition]对应的Scope作用于去进行解析
 *
 * @see ScopeMetadata
 */
fun interface ScopeMetadataResolver {

    /**
     * 根据[BeanDefinition]去解析出来该Bean对应的Scope
     *
     * @param definition BeanDefinition
     * @return 解析得到的Scope元信息
     */
    fun resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata
}