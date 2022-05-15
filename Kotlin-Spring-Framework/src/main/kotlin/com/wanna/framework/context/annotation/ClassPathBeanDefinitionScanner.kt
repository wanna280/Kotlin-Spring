package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.ScannedGenericBeanDefinition
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.core.util.AnnotationConfigUtils
import com.wanna.framework.core.util.ClassDiscoveryUtils

/**
 * 这是ClassPath下的BeanDefinition的Scanner，负责完成指定的包下的全部配置类的扫描，并将其注册到BeanDefinitionRegistry当中；
 *
 * 在指定的包下寻找组件，可以重写findCandidateComponents方法去进行自定义扫描的逻辑...
 */
open class ClassPathBeanDefinitionScanner(
    private val registry: BeanDefinitionRegistry,
    useDefaultFilters: Boolean,  // 是否需要应用默认的Filter？
) {
    // includeFilters
    private var includeFilters = ArrayList<TypeFilter>()

    // excludeFilters
    private var excludeFilters = ArrayList<TypeFilter>()

    // beanNameGenerator，可以允许外部访问，直接进行设置，默认为支持注解版的BeanNameGenerator
    private var beanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // environment，利用允许外部访问，直接去进行设置
    private var environment: Environment = this.getOrDefaultEnvironment(registry)

    // 是否包含注解版的配置？如果开启了，使用它进行扫描时，就会往容器中注册注解的通用处理器
    private var includeAnnotationConfig: Boolean = true

    private var lazyInit: Boolean = false

    init {
        // 是否要应用默认的Filter？去匹配Component组件
        if (useDefaultFilters) {
            this.registerDefaultFilters()
        }
    }

    /**
     * 注册默认的Filters
     */
    protected open fun registerDefaultFilters() {
        this.includeFilters += AnnotationTypeFilter(Component::class.java)
    }

    /**
     * 扫描指定的包中的BeanDefinition，并注册到容器当中，返回值为扫描到的BeanDefinition的数量
     */
    open fun scan(vararg packages: String): Int {
        val beforeCount = registry.getBeanDefinitionCount()  // before Count
        // 完成指定的所有的包的扫描，并直接注册到容器当中
        doScan(*packages)

        // 如果需要引入注解处理的相关配置，那么完成注解版的处理器的注册工作...
        if (includeAnnotationConfig) {
            AnnotationConfigUtils.registerAnnotationConfigProcessors(registry)
        }
        return registry.getBeanDefinitionCount() - beforeCount  // return modifyCount=afterCount-beforeCount
    }

    /**
     * doScan，获取到指定的包下的所有BeanDefinition
     *
     * @param packages 要去进行扫描的包
     * @return 包下扫描到的所有的BeanDefinition组件
     */
    open fun doScan(vararg packages: String): Set<BeanDefinitionHolder> {
        val beanDefinitions = HashSet<BeanDefinitionHolder>()
        packages.forEach { packageName ->
            // 获取要进行扫描的包中的所有候选BeanDefinition，这个方法，有可能会交给子类去进行重写
            // 比如MyBatis，就重写了这个方法去自定义逻辑，去完成Mapper的扫描...
            val candidateComponents = findCandidateComponents(packageName)
            candidateComponents
                .filter { isCandidateComponent(it.getBeanClass()) }  // 判断是否是候选Bean
                .forEach { beanDefinition ->
                    // 利用beanNameGenerator给beanDefinition生成beanName，并注册到BeanDefinitionRegistry当中
                    val beanName = beanNameGenerator.generateBeanName(beanDefinition, registry)

                    // 如果它是一个被注解标注的BeanDefinition，那么可以从它身上找到注解的相关的元信息
                    // 需要处理@Role/@Lazy/@DependsOn/@Primary注解
                    if (beanDefinition is AnnotatedBeanDefinition) {
                        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition)
                    }

                    if (lazyInit) {
                        beanDefinition.setLazyInit(true)
                    }
                    registry.registerBeanDefinition(beanName, beanDefinition)

                    // 加入到扫描到的BeanDefinition列表当中，封装成为BeanDefinitionHolder，让调用方可以获取到beanName
                    beanDefinitions += BeanDefinitionHolder(beanDefinition, beanName)
                }
        }
        return beanDefinitions
    }

    /**
     * 从指定的包下，扫描出来所有的BeanDefinitions列表，默认实现是创建ScannedGenericBeanDefinition
     *
     * @param packageName 指定的包名
     */
    open fun findCandidateComponents(packageName: String): Set<BeanDefinition> {
        return ClassDiscoveryUtils.scan(packageName).map { ScannedGenericBeanDefinition(it) }.toSet()
    }

    /**
     * 是否是候选的要导入的组件？使用includeFilter和excludeFilter去进行挨个匹配
     *
     * @param clazz 候选类
     */
    protected open fun isCandidateComponent(clazz: Class<*>?): Boolean {
        if (clazz == null) {
            return false
        }
        // 如果被excludeFilter匹配，直接return false
        this.excludeFilters.forEach {
            if (it.matches(clazz)) {
                return false
            }
        }
        // 如果被includeFilter匹配，return true
        this.includeFilters.forEach {
            if (it.matches(clazz)) {
                return true
            }
        }
        return false
    }

    /**
     * 获取或者是创建一个默认的Environment，
     * (1)如果Registry可以获取到Environment，那么直接从Registry当中去获取到Environment对象;
     * (2)如果获取不到，那么就创建一个默认的Environment
     */
    private fun getOrDefaultEnvironment(registry: BeanDefinitionRegistry): Environment {
        return if (registry is EnvironmentCapable) registry.getEnvironment() else StandardEnvironment()
    }

    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: AnnotationBeanNameGenerator.INSTANCE  // if null,use default
    }

    open fun setIncludeAnnotationConfig(includeAnnotationConfig: Boolean) {
        this.includeAnnotationConfig = includeAnnotationConfig
    }

    open fun addIncludeFilter(typeFilter: TypeFilter) {
        this.includeFilters += typeFilter
    }

    open fun addExcludeFilter(typeFilter: TypeFilter) {
        this.excludeFilters += typeFilter
    }

    open fun setLazyInit(lazyInit: Boolean) {
        this.lazyInit = lazyInit
    }
}