package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.annotation.Lookup
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.ScannedGenericBeanDefinition
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.ResourcePatternResolver
import com.wanna.framework.core.io.support.ResourcePatternResolver.Companion.CLASSPATH_ALL_URL_PREFIX
import com.wanna.framework.core.io.support.ResourcePatternUtils
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.ClassReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * 提供ClassPath下的候选组件的扫描的相关功能，负责将给定的包下的资源去全部加载成为BeanDefinition
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @see ClassPathBeanDefinitionScanner
 * @see ResourceLoader
 * @see ResourcePatternResolver
 */
open class ClassPathScanningCandidateComponentProvider(
    useDefaultFilters: Boolean = true,  // 是否需要应用默认的Filter？
    @Nullable resourceLoader: ResourceLoader? = null
) : ResourceLoaderAware, EnvironmentCapable {
    companion object {
        /**
         * 默认的资源的表达式的路径，表示通配
         */
        private const val DEFAULT_RESOURCE_PATTERN = "**/*.class"
    }

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // includeFilters
    private var includeFilters = ArrayList<TypeFilter>()

    // excludeFilters
    private var excludeFilters = ArrayList<TypeFilter>()

    /**
     * 解析资源的表达式，默认为通配
     */
    private var resourcePattern: String = DEFAULT_RESOURCE_PATTERN

    /**
     * Environment，Spring容器去进行自动注入
     */
    private var environment: Environment? = null

    /**
     * 资源的解析器，根据ResourceLoader去进行包装
     */
    private var resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)

    init {
        // 是否要应用默认的Filter？默认情况下，需要去匹配@Component的Bean
        if (useDefaultFilters) {
            this.registerDefaultFilters()
        }
    }

    /**
     * 给定一个候选的包，去扫描该包下的候选的组件
     *
     * @param basePackage 要扫描的包？(支持去进行占位符解析)
     * @return 该包下扫描得到的所有的候选BeanDefinition
     */
    open fun scanCandidateComponents(basePackage: String): Set<BeanDefinition> {
        val candidates = LinkedHashSet<BeanDefinition>()
        try {
            val packageSearchPath = CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage) + '/' + resourcePattern
            val resources = getResourcePatternResolver().getResources(packageSearchPath)
            resources.forEach {
                if (logger.isTraceEnabled) {
                    logger.trace("Scanning resource... [$it]")
                }
                if (it.isReadable()) {
                    try {
                        val className =
                            ClassUtils.convertResourcePathToClassName(ClassReader(it.getInputStream()).className)
                        val classLoader = resourcePatternResolver.getClassLoader() ?: ClassUtils.getDefaultClassLoader()
                        val clazz = classLoader.loadClass(className)

                        // 对clazz利用IncludeFilter/ExcludeFilter去进行检查，是否符合给定条件
                        if (isCandidateComponent(clazz)) {
                            val beanDefinition = ScannedGenericBeanDefinition(clazz)
                            beanDefinition.setSource(it)
                            beanDefinition.setResource(it)  // set Resource

                            // 对BeanDefinition去进行检查，排除掉抽象类
                            if (isCandidateComponent(beanDefinition)) {
                                if (logger.isDebugEnabled) {
                                    logger.debug("Resource [$it] will be a candidate BeanDefinition to register")
                                }
                                candidates += beanDefinition
                            } else {
                                if (logger.isDebugEnabled) {
                                    logger.debug("Resource of abstract class $it has been filtered")
                                }
                            }
                        } else {
                            if (logger.isTraceEnabled) {
                                logger.trace("Ignore cannot match by filter resource $it")
                            }
                        }
                    } catch (ex: Throwable) {
                        // temp ignore LinkageError
                        if (ex !is NoClassDefFoundError) {
                            throw IllegalStateException("Cannot read given resource [$it]", ex)
                        }
                    }
                } else {
                    if (logger.isTraceEnabled) {
                        logger.debug("Ignore a cannot readable resource [$it]")
                    }
                }
            }
        } catch (ex: IOException) {
            throw IllegalStateException("Read resource file Error", ex)
        }
        return candidates
    }

    /**
     * 排除掉所有的抽象的类的BeanDefinition
     *
     * @param definition BeanDefinition
     */
    protected open fun isCandidateComponent(definition: AnnotatedBeanDefinition): Boolean {
        val metadata = definition.getMetadata()
        return !definition.isAbstract() ||
                definition.isAbstract() && metadata.hasAnnotatedMethods(Lookup::class.java.name)
    }


    /**
     * 自动注入ResourceLoader
     *
     * @param resourceLoader ResourceLoader
     */
    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
    }

    open fun getResourcePatternResolver() = this.resourcePatternResolver

    open fun setResourcePatternResolver(resolver: ResourcePatternResolver) {
        this.resourcePatternResolver = resolver
    }

    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun setResourcePattern(resourcePattern: String) {
        this.resourcePattern = resourcePattern
    }

    override fun getEnvironment() = this.environment ?: StandardEnvironment()


    /**
     * 注册默认的Filters
     *
     * @see AnnotationTypeFilter
     * @see Component
     */
    protected open fun registerDefaultFilters() {
        this.includeFilters += AnnotationTypeFilter(Component::class.java)
    }

    /**
     * 是否是候选的要导入的组件？使用includeFilter和excludeFilter去进行挨个匹配
     *
     * @param clazz 候选类
     * @return 该类是否是候选的组件？
     */
    protected open fun isCandidateComponent(clazz: Class<*>?): Boolean {
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

    open fun addIncludeFilter(typeFilter: TypeFilter) {
        this.includeFilters += typeFilter
    }

    open fun addExcludeFilter(typeFilter: TypeFilter) {
        this.excludeFilters += typeFilter
    }

    /**
     * 重设所有的Filter，如果必要的话，还需要去应用默认的Filter
     *
     * @param useDefaultFilters 是否需要应用默认的Filter？
     */
    open fun resetFilters(useDefaultFilters: Boolean) {
        this.includeFilters.clear()
        this.excludeFilters.clear()
        if (useDefaultFilters) {
            registerDefaultFilters()
        }
    }

    /**
     * 将basePackage去进行占位符解析，并得到资源路径
     *
     * @param basePackage basePackage
     * @return 该包名最终解析得到的资源路径名
     */
    protected open fun resolveBasePackage(basePackage: String): String {
        return ClassUtils.convertClassNameToResourcePath(getEnvironment().resolvePlaceholders(basePackage)!!)
    }

}