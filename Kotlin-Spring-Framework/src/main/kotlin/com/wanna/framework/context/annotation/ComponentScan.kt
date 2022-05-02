package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 扫描指定的包下的所有类下的标注了Component的组件
 */
@Repeatable
annotation class ComponentScan(
    /**
     * 要扫描哪些包？和basePackages一致
     *
     * @see basePackageClasses
     * @see basePackages
     */
    @get:AliasFor("basePackages")
    val value: Array<String> = [],

    /**
     * 要扫描哪些包？
     */
    @get:AliasFor("value")
    val basePackages: Array<String> = [],

    /**
     * 要以哪些类的所在包去进行扫描
     */
    val basePackageClasses: Array<KClass<*>> = [],

    /**
     * 扫描过程当中需要使用的beanNameGenerator
     */
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,
)
