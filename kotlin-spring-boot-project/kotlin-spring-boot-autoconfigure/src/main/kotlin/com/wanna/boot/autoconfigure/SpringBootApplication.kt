package com.wanna.boot.autoconfigure

import com.wanna.boot.SpringBootConfiguration
import com.wanna.boot.context.TypeExcludeFilter
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.ComponentScan.Filter
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.FilterType.*
import com.wanna.framework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 标识这是一个SpringBootApplication, 它支持完成自动配置以及包的扫描
 *
 * @param proxyBeanMethods 是否要代理目标类？
 * @param nameGenerator BeanNameGenerator
 * @param scanBasePackages ComponentScan要去进行扫描的包
 * @param scanBasePackageClasses ComponentScan要去进行扫描的包(以类的方式给出)
 * @param excludeNames 要去排除的自动配置类的类名
 * @param exclude 要去进行排除的自动配置类
 */
@ComponentScan(
    excludeFilters = [Filter(CUSTOM, [TypeExcludeFilter::class]),
        Filter(CUSTOM, [AutoConfigurationExcludeFilter::class])]
)  // ComponentScan, 完成包的扫描, 并排除掉所有的自动配置类, 避免自动配置类被扫描到
@SpringBootConfiguration  // 标识这是一个SpringBoot的配置类
@EnableAutoConfiguration  // 自动配置, 使用DeferredImportSelector去完成
annotation class SpringBootApplication(
    @get:AliasFor(annotation = EnableAutoConfiguration::class, value = "exclude")
    val exclude: Array<KClass<*>> = [],

    @get:AliasFor(annotation = EnableAutoConfiguration::class, value = "excludeNames")
    val excludeNames: Array<String> = [],

    @get:AliasFor(annotation = ComponentScan::class, value = "basePackages")
    val scanBasePackages: Array<String> = [],

    @get:AliasFor(annotation = ComponentScan::class, value = "basePackageClasses")
    val scanBasePackageClasses: Array<KClass<*>> = [],

    @get:AliasFor(annotation = ComponentScan::class, value = "nameGenerator")
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,

    @get:AliasFor(annotation = Configuration::class, value = "proxyBeanMethods")
    val proxyBeanMethods: Boolean = true
)