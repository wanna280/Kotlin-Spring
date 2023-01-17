package com.wanna.mybatis.spring.annotation

import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.Import
import com.wanna.mybatis.spring.mapper.MapperFactoryBean
import kotlin.reflect.KClass

/**
 * MapperScan, 负责扫描指定的包下的Mapper, 默认情况下只要是该包下的一个接口, 就能去被扫描成为一个Mapper
 *
 * @see MapperScannerRegistrar
 *
 * @param value 要扫描的包(同basePackages)
 * @param basePackages 要扫描的包(同value)
 * @param basePackageClasses 要扫描的指定类下的包
 * @param nameGenerator beanNameGenerator
 * @param sqlSessionFactoryRef SqlSessionFactoryBean的beanName
 * @param sqlSessionTemplateRef SqlSessionTemplate的beanName
 * @param factoryBean 导入Mapper使用的FactoryBean
 * @param defaultScope 默认的作用域(默认为单例)
 * @param lazyInitialization 是否是懒加载的?
 * @param annotationClass 要匹配哪个注解? 在拥有注解的情况下, 就将其扫描到容器当中
 * @param markerInterface 要扫描哪个接口的所有子类? 可以和annotationClass一起使用, 两者之间是取并集, 而不是取交集
 */
@Import([MapperScannerRegistrar::class])
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class MapperScan(
    val value: Array<String> = [],
    val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = [],
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,
    val annotationClass: KClass<out Annotation> = Annotation::class,
    val sqlSessionTemplateRef: String = "",
    val sqlSessionFactoryRef: String = "",
    val factoryBean: KClass<out MapperFactoryBean> = MapperFactoryBean::class,
    val defaultScope: String = "",
    val lazyInitialization: String = "",
    val markerInterface: KClass<*> = Class::class
)
