package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 注解版的Scope的解析器，解析@Scope注解当中的相关环境，并封装成为ScopeMetadata
 *
 * @see Scope
 * @see ScopeMetadataResolver
 */
open class AnnotationScopeMetadataResolver : ScopeMetadataResolver {

    var scopeAnnotationType = Scope::class.java

    override fun resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata {
        if (definition is AnnotatedBeanDefinition) {
            val attributes = definition.getMetadata().getAnnotationAttributes(scopeAnnotationType)
            if (attributes.isNotEmpty()) {
                val scopeName = attributes["scopeName"].toString()
                val proxyMode = attributes["proxyMode"] as ScopedProxyMode
                return ScopeMetadata(scopeName, proxyMode)
            }
        }
        return ScopeMetadata()
    }
}