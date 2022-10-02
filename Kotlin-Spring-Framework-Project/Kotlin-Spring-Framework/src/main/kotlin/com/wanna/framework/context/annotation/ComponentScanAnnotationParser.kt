package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.core.type.filter.AssignableTypeFilter
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.util.ClassUtils

/**
 * 这是完成ComponentScan注解的扫描的解析器，负责将@ComponentScan注解当中配置的属性去进行解析，并完成ComponentScan的组件的扫描
 *
 * @see ClassPathBeanDefinitionScanner
 * @see ComponentScan
 */
@Suppress("UNCHECKED_CAST")
open class ComponentScanAnnotationParser(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    private val classLoader: ClassLoader,
    private val componentScanBeanNameGenerator: BeanNameGenerator
) {
    open fun parse(attributes: AnnotationAttributes, className: String): Set<BeanDefinitionHolder> {
        val useDefaultFilters = attributes.getBoolean("useDefaultFilters")
        val scanner = ClassPathBeanDefinitionScanner(registry, useDefaultFilters)

        // 设置beanNameGenerator
        scanner.setBeanNameGenerator(componentScanBeanNameGenerator)

        val packages = ArrayList<String>()
        packages.addAll(attributes.getStringArray("basePackages"))
        packages.addAll((attributes.getClassArray("basePackageClasses")).map { it.packageName }.toList())

        // 添加includeFilters/excludeFilters到Scanner当中
        val includeFilters = attributes["includeFilters"] as Array<ComponentScan.Filter>
        val excludeFilters = attributes["excludeFilters"] as Array<ComponentScan.Filter>
        getTypeFilters(includeFilters).forEach(scanner::addIncludeFilter)
        getTypeFilters(excludeFilters).forEach(scanner::addExcludeFilter)

        // 设置是否懒加载？
        val lazyInit = attributes.getBoolean("lazyInit")
        scanner.setLazyInit(lazyInit)

        // 如果没有获取到配置的packages列表，那么使用配置类所在的packageName作为要扫描的包
        if (packages.isEmpty()) {
            packages += ClassUtils.getPackageName(className)
        }

        // 使用类路径下的BeanDefinitionScanner去进行扫描
        return scanner.doScan(*packages.toTypedArray())
    }

    /**
     * 获取TypeFilter列表
     */
    private fun getTypeFilters(filters: Array<ComponentScan.Filter>): List<TypeFilter> {
        val typeFilters = ArrayList<TypeFilter>()
        AnnotationAttributesUtils.asAnnotationAttributesSet(*filters).forEach { attr ->
            val filterType = attr["filterType"] as FilterType
            val classArray = attr.getClassArray("classes")
            classArray.forEach {
                when (filterType) {
                    FilterType.ANNOTATION -> typeFilters += AnnotationTypeFilter(it as Class<out Annotation>)
                    FilterType.ASSIGNABLE_TYPE -> typeFilters += AssignableTypeFilter(it)
                    FilterType.CUSTOM -> typeFilters += ParserStrategyUtils.instanceClass<TypeFilter>(
                        it, environment, registry
                    )
                }
            }
        }
        return typeFilters
    }
}