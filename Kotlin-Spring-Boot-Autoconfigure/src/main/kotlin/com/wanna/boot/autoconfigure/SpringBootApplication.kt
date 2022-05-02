package com.wanna.boot.autoconfigure

import com.wanna.boot.SpringBootConfiguration
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ComponentScan
import com.wanna.framework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 标识这是一个SpringBootApplication，它支持完成自动配置以及包的扫描
 */
@ComponentScan  // ComponentScan，完成包的扫描
@SpringBootConfiguration  // 标识这是一个SpringBoot的配置类
@EnableAutoConfiguration  // 自动配置，使用DeferredImportSelector去完成
annotation class SpringBootApplication(
    /**
     * 自动配置过程当中，需要排除哪些配置类？以kclass的方式给出
     *
     * @see EnableAutoConfiguration.exclude
     */
    @get:AliasFor(annotation = EnableAutoConfiguration::class, value = "exclude")
    val exclude: Array<KClass<*>> = [],
    /**
     * 自动配置过程中，需要排除哪些配置类？以类名的方式给出
     *
     * @see EnableAutoConfiguration.excludeNames
     */
    @get:AliasFor(annotation = EnableAutoConfiguration::class, value = "excludeNames")
    val excludeNames: Array<String> = [],

    /**
     * ComponentScan要扫描的包
     *
     * @see ComponentScan.basePackages
     */
    @get:AliasFor(annotation = ComponentScan::class, value = "basePackages")
    val scanBasePackages: Array<String> = [],

    /**
     * ComponentScan要扫描的包
     *
     * @see ComponentScan.basePackageClasses
     */
    @get:AliasFor(annotation = ComponentScan::class, value = "basePackageClasses")
    val scanBasePackageClasses: Array<KClass<*>> = [],

    /**
     * beanNameGenerator
     *
     * @see ComponentScan.nameGenerator
     */
    @get:AliasFor(annotation = ComponentScan::class, value = "nameGenerator")
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,

    /**
     * 是否要代理目标类？
     *
     * @see Configuration.proxyBeanMethods
     */
    @get:AliasFor(annotation = Configuration::class, value = "proxyBeanMethods")
    val proxyBeanMethods: Boolean = true
)
