package com.wanna.framework.context.annotation

import com.wanna.framework.aop.config.AopConfigUtils
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * AopProxy的注册器, 负责给Spring当中注册一个AutoProxy的代理创建器, 为SpringAop提供支持
 *
 * @see com.wanna.framework.transaction.annotation.EnableTransactionManagement
 */
open class AutoProxyRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry)
    }
}