package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AnnotationConfigUtils
import com.wanna.framework.util.AnnotationConfigUtils.registerAnnotationConfigProcessors

/**
 * 这是ClassPath下的BeanDefinition的Scanner，负责完成指定的包下的全部配置类的扫描，并将其注册到BeanDefinitionRegistry当中；
 *
 * 在指定的包下寻找Spring的配置类，可以重写findCandidateComponents方法去进行自定义扫描的逻辑...
 *
 * @param registry BeanDefinitionRegistry(需要去注册BeanDefinition的地方)
 * @param useDefaultFilters 是否需要去应用默认的Filter？
 * @param resourceLoader ResourceLoader，提供资源的加载(可以为null)
 */
open class ClassPathBeanDefinitionScanner(
    val registry: BeanDefinitionRegistry,
    useDefaultFilters: Boolean = true,
    @Nullable resourceLoader: ResourceLoader? = null
) : ClassPathScanningCandidateComponentProvider(useDefaultFilters, resourceLoader) {

    /**
     * beanNameGenerator，默认为支持注解版的BeanNameGenerator
     */
    private var beanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE


    /**
     * 是否包含注解版的配置？如果开启了，使用它进行扫描时，就会往容器中注册注解的通用处理器(默认为true)
     */
    private var includeAnnotationConfig: Boolean = true

    /**
     * Scope的Metadata的Resolver，提供对于`@Scope`注解的解析
     */
    private var scopeMetadataResolver: ScopeMetadataResolver = AnnotationScopeMetadataResolver()

    /**
     * 扫描进来的Bean是否需要设置成为lazyInit的？
     */
    private var lazyInit: Boolean = false

    init {
        this.setEnvironment(getOrDefaultEnvironment(registry))
    }

    /**
     * 获取或者是创建一个默认的Environment，
     * (1)如果Registry可以获取到Environment，那么直接从Registry当中去获取到Environment对象;
     * (2)如果获取不到，那么就创建一个默认的Environment
     *
     * @param registry BeanDefinitionRegistry
     * @return 获取(创建)的Environment
     */
    private fun getOrDefaultEnvironment(registry: BeanDefinitionRegistry): Environment {
        return if (registry is EnvironmentCapable) registry.getEnvironment() else StandardEnvironment()
    }

    /**
     * 扫描指定的包中的BeanDefinition，并注册到容器当中，返回值为扫描到的BeanDefinition的数量
     *
     * @param packages 要去进行扫描的包的列表
     * @return 从给定的包当中去扫描到的BeanDefinition的数量
     */
    open fun scan(vararg packages: String): Int {
        val beforeCount = registry.getBeanDefinitionCount()  // before Count
        // 完成指定的所有的包的扫描，并直接注册到容器当中
        doScan(*packages)

        // 如果需要引入注解处理的相关配置，那么完成注解版的处理器的注册工作...
        if (includeAnnotationConfig) {
            registerAnnotationConfigProcessors(registry)
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
            candidateComponents.forEach { beanDefinition ->
                // 利用beanNameGenerator给beanDefinition生成beanName，并注册到BeanDefinitionRegistry当中
                val beanName = beanNameGenerator.generateBeanName(beanDefinition, registry)

                // 如果它是一个被注解标注的BeanDefinition，那么可以从它身上找到注解的相关的元信息
                // 需要处理@Role/@Lazy/@DependsOn/@Primary注解
                if (beanDefinition is AnnotatedBeanDefinition) {
                    AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition)
                }

                // 解析到BeanDefinition的Scope并且设置到BeanDefinition当中
                val metadata = scopeMetadataResolver.resolveScopeMetadata(beanDefinition)
                beanDefinition.setScope(metadata.scopeName)

                // set lazyInit if necessary
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
     * @param packageName 指定需要去进行扫描BeanDefinition的包名
     * @return 指定的包下扫描得到的所有的BeanDefinition的列表
     */
    open fun findCandidateComponents(packageName: String): Set<BeanDefinition> {
        return scanCandidateComponents(packageName)
    }

    /**
     * 设置BeanNameGenerator
     *
     * @param beanNameGenerator 你要使用的BeanNameGenerator，可以为空，代表使用默认值
     */
    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: AnnotationBeanNameGenerator.INSTANCE  // if null,use default
    }

    /**
     * 是否需要将注解相关的配置去导入进来？
     *
     * @param includeAnnotationConfig 如果为true，将会导入Spring的一堆注解处理器
     */
    open fun setIncludeAnnotationConfig(includeAnnotationConfig: Boolean) {
        this.includeAnnotationConfig = includeAnnotationConfig
    }

    /**
     * 扫描进来的Bean是否都需要配置成为懒加载的？
     *
     * @param lazyInit lazyInit
     */
    open fun setLazyInit(lazyInit: Boolean) {
        this.lazyInit = lazyInit
    }

    /**
     * 是否需要去进行懒加载?
     *
     * @return lazyInit or not
     */
    open fun isLazyInit(): Boolean = this.lazyInit

    /**
     * 自定义ScopeResolver
     *
     * @param scopeMetadataResolver ScoprResolver
     */
    open fun setScopeResolver(scopeMetadataResolver: ScopeMetadataResolver) {
        this.scopeMetadataResolver = scopeMetadataResolver
    }

    /**
     * 获取ScopeResolver
     *
     * @return ScopeResolver
     */
    open fun getScopeResolver(): ScopeMetadataResolver = this.scopeMetadataResolver

    /**
     * 自定义默认的ScopedProxyMode，对于默认配置下的所有的Bean的作用域都将会被设置成为该ScopedProxyMode
     *
     * @param scopedProxyMode ScopedProxyMode
     */
    open fun setScopedProxyMode(scopedProxyMode: ScopedProxyMode) {
        this.scopeMetadataResolver = AnnotationScopeMetadataResolver(scopedProxyMode)
    }
}