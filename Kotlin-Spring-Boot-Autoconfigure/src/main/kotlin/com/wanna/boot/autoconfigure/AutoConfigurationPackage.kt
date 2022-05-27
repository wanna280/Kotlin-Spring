package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 这是一个自动配置包的注解，标识这个注解可以完成自动配置包；
 * 它的主要作用是将"basePackages"和"basePackageClasses"导入的包的列表，注册到SpringBeanFactory当中；
 *
 * @see EnableAutoConfiguration
 */
@Import([AutoConfigurationPackages.Registrar::class])
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class AutoConfigurationPackage(
    val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = [],
)
