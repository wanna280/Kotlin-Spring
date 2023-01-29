package com.wanna.framework.beans.factory.support

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.PathMatchingResourcePatternResolver
import com.wanna.framework.core.io.support.ResourcePatternResolver

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
abstract class AbstractBeanDefinitionReader(private val registry: BeanDefinitionRegistry) : BeanDefinitionReader,
    EnvironmentCapable {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * BeanNameGenerator
     */
    private var beanNameGenerator: BeanNameGenerator = DefaultBeanNameGenerator.INSTANCE

    /**
     * BeanClassLoader
     */
    private var beanClassLoader: ClassLoader? = null

    /**
     * ResourceLoader
     */
    private var resourceLoader: ResourceLoader? = null

    /**
     * Environment
     */
    private var environment: Environment? = null

    init {
        if (registry is ResourceLoader) {
            this.resourceLoader = registry
        } else {
            this.resourceLoader = PathMatchingResourcePatternResolver()
        }
        this.setBeanClassLoader(this.resourceLoader!!.getClassLoader())

        if (registry is EnvironmentCapable) {
            this.environment = registry.getEnvironment()
        } else {
            this.environment = StandardEnvironment()
        }
    }

    override fun getEnvironment() = this.environment ?: throw IllegalStateException("环境不能为空")

    override fun getRegistry() = this.registry

    override fun getBeanClassLoader() = this.beanClassLoader

    override fun getResourceLoader(): ResourceLoader? = this.resourceLoader

    override fun getBeanNameGenerator() = this.beanNameGenerator

    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: DefaultBeanNameGenerator.INSTANCE
    }

    open fun setResourceLoader(resourceLoader: ResourceLoader?) {
        this.resourceLoader = resourceLoader
        this.beanClassLoader = resourceLoader?.getClassLoader()
    }

    open fun setBeanClassLoader(beanClassLoader: ClassLoader?) {
        this.beanClassLoader = beanClassLoader
    }


    override fun loadBeanDefinitions(vararg resources: Resource) = resources.sumOf { loadBeanDefinitions(it) }

    override fun loadBeanDefinitions(vararg locations: String) = locations.sumOf { loadBeanDefinitions(it) }

    override fun loadBeanDefinitions(location: String) = loadBeanDefinitions(location, null)

    /**
     * 根据location去加载BeanDefinition
     *
     * @param location 资源路径
     * @param actualResources 真正的资源列表, 对于加载到的资源将会存放到这个列表当中
     * @return 加载到的BeanDefinition的数量
     */
    open fun loadBeanDefinitions(location: String, actualResources: MutableSet<Resource>?): Int {
        val loader = resourceLoader ?: throw IllegalStateException("ResourceLoader不能为空")

        if (loader is ResourcePatternResolver) {
            val resources = loader.getResources(location)
            val count = loadBeanDefinitions(*resources)
            actualResources?.addAll(resources)
            if (logger.isTraceEnabled) {
                logger.trace("从location pattern=[$location]去加载到[$count]个BeanDefinition")
            }
            return count
        } else {
            val resource = loader.getResource(location)
            val count = loadBeanDefinitions(resource)
            actualResources?.add(resource)
            if (logger.isTraceEnabled) {
                logger.trace("从location=[$location]去加载到[$count]个BeanDefinition")
            }
            return count
        }
    }
}