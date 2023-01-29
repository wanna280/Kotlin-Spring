package com.wanna.framework.context.annotation

/**
 * 开启AspectJAutoProxy的相关功能, 通过AspectJAutoProxyRegistrar, 给容器中注册AspectJ相关的注解处理器
 *
 * @see AspectJAutoProxyRegistrar
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import([AspectJAutoProxyRegistrar::class])
annotation class EnableAspectJAutoProxy(

    // 是否要代理目标类? 如果为true时, 会使用CGLIB去生成代理
    val proxyTargetClass: Boolean = false,

    // 是否要暴露代理给AopUtil? 默认为false
    val exposeProxy: Boolean = false
)
