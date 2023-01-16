package com.wanna.framework.context.annotation

import com.wanna.framework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 扫描指定的包下的所有类下匹配的注解(不一定只扫描某个类型的组件, 也可以自己去通过Filter去自己指定类型)
 *
 * @see ComponentScanAnnotationParser
 * @see ClassPathBeanDefinitionScanner
 *
 * @param value  要扫描哪些包？具体的作用和basePackages一致, see[basePackages]
 * @param basePackages 要扫描哪些包？具体作用和value一致, see [value]
 * @param basePackageClasses 要以哪些类的所在包去进行扫描
 * @param nameGenerator 扫描过程当中需要使用的beanNameGenerator去生成beanName
 * @param scopeResolver  ScopeResolver, 提供对于Bean的Scope的解析工作, 默认实现为找到@Scope注解去进行使用; (Note: 只有在ScopeProxyMode没有自定义的情况下才会生效)
 * @param scopeProxy 自定义ScopeProxy, 对于@ComponentScan扫描进来的全部Bean(在没有特殊配置@Scope的情况下)的作用域都将会被设置成为它
 * @param includeFilters 需要匹配的条件才能导入进来的Filter, 只要匹配其中一个条件, 就支持被扫描进来
 * @param excludeFilters 匹配其中一个条件就不能导入的排除的Filter
 * @param useDefaultFilters 是否需要使用默认的Filter来匹配@Component相关注解, 默认为true
 * @param lazyInit 是否要将导入进来的Bean全部都设置成为懒加载？
 */
@Repeatable
annotation class ComponentScan(
    @get:AliasFor("basePackages") val value: Array<String> = [],
    @get:AliasFor("value") val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = [],
    val nameGenerator: KClass<out BeanNameGenerator> = BeanNameGenerator::class,
    val scopeResolver: KClass<out ScopeMetadataResolver> = AnnotationScopeMetadataResolver::class,
    val scopeProxy: ScopedProxyMode = ScopedProxyMode.DEFAULT,
    val includeFilters: Array<Filter> = [],
    val excludeFilters: Array<Filter> = [],
    val useDefaultFilters: Boolean = true,
    val lazyInit: Boolean = false
) {

    /**
     * ComponentScan匹配BeanDefinition过程当中使用到的Filter;
     * (1)当FilterType=ANNOTATION时, value指定的是注解类型
     * (2)当FilterType=ASSIGNABLE_TYPE时, value指定的是要匹配的clazz类型
     * (3)当FilterType=CUSTOM时, value指定的是自定义的TypeFilter的类型
     *
     * @see com.wanna.framework.core.type.filter.TypeFilter
     * @see com.wanna.framework.core.type.filter.AnnotationTypeFilter
     * @see com.wanna.framework.core.type.filter.AssignableTypeFilter
     * @see com.wanna.framework.core.type.filter.RegexPatternTypeFilter
     *
     * @param filterType  // 要去进行匹配的类型？匹配注解？匹配类型？
     * @param value 想要当做Filter的类
     * @param classes 想要当做Filter的类
     */
    annotation class Filter(
        val filterType: FilterType = FilterType.ANNOTATION,
        @get:AliasFor("classes") val value: Array<KClass<*>> = [],
        @get:AliasFor("value") val classes: Array<KClass<*>> = []
    )
}
