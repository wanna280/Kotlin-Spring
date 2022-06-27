package com.wanna.framework.context.annotation

import com.wanna.framework.aop.config.AopConfigUtils
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个AspectJAutoProxy的注册器，负责给容器中导入组件导入注解版的AspectJ的AOP代理的处理器
 */
open class AspectJAutoProxyRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(
        annotationMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry
    ) {

        // 给容器中注册一个AnnotationAwareAspectJAutoProxy组件，完成AOP代理，并支持注解版的AspectJ方式去完成代理
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry)

        // 获取EnableAspectJAutoProxy的注解信息
        val annotationAttributes = annotationMetadata.getAnnotationAttributes(EnableAspectJAutoProxy::class.java)
        if (annotationAttributes["proxyTargetClass"] == true) {
            AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry)
        }
        if (annotationAttributes["exposeProxy"] == true) {
            AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry)
        }
    }
}