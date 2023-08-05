package com.wanna.boot.devtools.restart

import com.wanna.framework.context.annotation.Conditional

/**
 * 只有在Restarter已经完成初始化工作之后才会被自动装配该Bean
 *
 * @see OnInitializedRestarterCondition
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Conditional(OnInitializedRestarterCondition::class)
annotation class ConditionalOnInitializedRestarter
