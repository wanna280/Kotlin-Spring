package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 标识这是一个条件，针对Bean去完成条件装配
 */
interface Condition {

    /**
     * @param context 条件的上下文信息，内部保存了BeanDefinitionRegistry/BeanFactory/Environment/ClassLoader等信息
     * @param metadata 注解的元信息
     */
    fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata) : Boolean
}