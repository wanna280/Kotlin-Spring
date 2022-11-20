package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 用于去对Bean去完成条件装配的[Condition]
 */
fun interface Condition {

    /**
     * @param context 条件的上下文信息，内部保存了BeanDefinitionRegistry/BeanFactory/Environment/ClassLoader等信息
     * @param metadata 注解的元信息
     */
    fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean
}