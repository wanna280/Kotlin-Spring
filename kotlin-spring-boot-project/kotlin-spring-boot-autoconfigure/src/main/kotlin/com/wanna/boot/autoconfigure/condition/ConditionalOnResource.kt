package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional

/**
 * 只有在给定的资源存在的情况下, 才会对该Bean去进行装配
 *
 * @see Conditional
 * @see OnResourceCondition
 *
 * @param resources 需要去进行匹配的资源文件
 */
@Conditional(OnResourceCondition::class)
annotation class ConditionalOnResource(vararg val resources: String)
