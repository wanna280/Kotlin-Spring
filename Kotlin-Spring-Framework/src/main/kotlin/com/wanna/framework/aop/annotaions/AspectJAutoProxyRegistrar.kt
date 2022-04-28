package com.wanna.framework.aop.annotaions

import com.wanna.framework.aop.config.AopConfigUtils
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.ImportBeanDefinitionRegistrar
import com.wanna.framework.context.annotations.BeanNameGenerator
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个AspectJAutoProxy的注册器，给容器中导入组件
 */
class AspectJAutoProxyRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(
        annotationMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        beanNameGenerator: BeanNameGenerator
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