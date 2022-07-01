package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * ScopeMetadata信息
 */
open class ScopeMetadata(
    var scopeName: String = BeanDefinition.SCOPE_SINGLETON,
    var scopeMode: ScopedProxyMode = ScopedProxyMode.DEFAULT
)