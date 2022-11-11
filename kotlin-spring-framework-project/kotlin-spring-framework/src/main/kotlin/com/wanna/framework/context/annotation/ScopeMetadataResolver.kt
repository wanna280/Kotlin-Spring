package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * ScopeMetadata的解析器，负责解析一个BeanDefinition当中的类型的Scope
 */
interface ScopeMetadataResolver {
    fun resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata
}