package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 注解版的Scope的解析器，解析@Scope注解当中的相关环境，并封装成为ScopeMetadata
 *
 * @see Scope
 * @see ScopeMetadataResolver
 */
open class AnnotationScopeMetadataResolver(val defaultProxyMode: ScopedProxyMode) : ScopeMetadataResolver {
    constructor() : this(ScopedProxyMode.NO)

    /**
     * Scope注解
     */
    var scopeAnnotationType = Scope::class.java

    override fun resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata {
        if (definition is AnnotatedBeanDefinition) {
            val attributes = definition.getMetadata().getAnnotations().get(scopeAnnotationType)
            if (attributes.present) {
                val scopeName = attributes.getString("scopeName")
                var proxyMode = attributes.getEnum("proxyMode", ScopedProxyMode::class.java)
                if (proxyMode == ScopedProxyMode.DEFAULT) {
                    proxyMode = defaultProxyMode
                }
                return ScopeMetadata(scopeName, proxyMode)
            }
        }
        return ScopeMetadata()
    }
}