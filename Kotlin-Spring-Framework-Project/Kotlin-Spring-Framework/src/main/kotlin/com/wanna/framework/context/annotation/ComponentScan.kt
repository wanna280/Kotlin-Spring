package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 扫描指定的包下的所有类下匹配的注解(不一定只扫描某个类型的组件，也可以自己去通过Filter去自己指定类型)
 *
 * @see ComponentScanAnnotationParser
 * @see ClassPathBeanDefinitionScanner
 */
@Repeatable
annotation class ComponentScan(
    /**
     * 要扫描哪些包？具体的作用和basePackages一致
     *
     * @see basePackageClasses
     * @see basePackages
     */
    @get:AliasFor("basePackages") val value: Array<String> = [],

    /**
     * 要扫描哪些包？具体作用和value一直
     */
    @get:AliasFor("value") val basePackages: Array<String> = [],

    /**
     * 要以哪些类的所在包去进行扫描
     */
    val basePackageClasses: Array<KClass<*>> = [],

    /**
     * 扫描过程当中需要使用的beanNameGenerator去生成beanName
     */
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,

    /**
     * 需要匹配的条件才能导入进来的Filter，只要匹配其中一个条件，就支持被扫描进来
     */
    val includeFilters: Array<Filter> = [],

    /**
     * 匹配其中一个条件就不能导入的排除的Filter
     */
    val excludeFilters: Array<Filter> = [],

    /**
     * 是否需要使用默认的Filter来匹配@Component相关注解，默认为true
     */
    val useDefaultFilters: Boolean = true,

    /**
     * 是否要将导入进来的Bean全部都设置成为懒加载？
     */
    val lazyInit: Boolean = false
) {

    /**
     * ComponentScan匹配BeanDefinition过程当中使用到的Filter；
     * (1)当FilterType=ANNOTATION时，value指定的是注解类型
     * (2)当FilterType=ASSIGNABLE_TYPE时，value指定的是要匹配的clazz类型
     * (3)当FilterType=CUSTOM时，value指定的是自定义的TypeFilter的类型
     */
    annotation class Filter(
        val filterType: FilterType = FilterType.ANNOTATION,  // 要去进行匹配的类型？匹配注解？匹配类型？
        @get:AliasFor("classes") val value: Array<KClass<*>> = [],  // 想要当做Filter的类
        @get:AliasFor("value") val classes: Array<KClass<*>> = []   // 想要当做Filter的类
    )
}
