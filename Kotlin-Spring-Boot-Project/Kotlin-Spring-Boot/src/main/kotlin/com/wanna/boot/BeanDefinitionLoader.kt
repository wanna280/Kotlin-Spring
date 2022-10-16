package com.wanna.boot

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.xml.XmlBeanDefinitionReader
import com.wanna.framework.context.annotation.AnnotatedBeanDefinitionReader
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ClassPathBeanDefinitionScanner
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.ClassPathResource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.support.PathMatchingResourcePatternResolver
import com.wanna.framework.util.ClassUtils
import java.io.IOException

/**
 * 这是一个BeanDefinition的Loader，它内部继承了AnnotatedBeanDefinitionReader，去完成BeanDefinition的加载；
 * 它注册是加载注册到SpringApplication当中的一些配置类的对象，比如primarySources等
 *
 * @see SpringApplication.primarySources
 * @see AnnotatedBeanDefinitionReader
 * @param registry BeanDefinitionRegistry
 * @param sources 要注册的配置类列表
 */
open class BeanDefinitionLoader(registry: BeanDefinitionRegistry, private val sources: Array<*>) {

    /**
     * BeanNameGenerator，用于BeanName的生成
     */
    private var beanNameGenerator: BeanNameGenerator? = null

    /**
     * Environment
     */
    private var environment: ConfigurableEnvironment? = null

    /**
     * ResourceLoader
     */
    private var resourceLoader: ResourceLoader? = null

    /**
     * BeanDefinitionScanner，提供包的扫描
     */
    private var scanner: ClassPathBeanDefinitionScanner = ClassPathBeanDefinitionScanner(registry)

    /**
     * 这是一个BeanDefinitionReader，负责注册配置类到容器当中
     */
    private val reader = AnnotatedBeanDefinitionReader(registry)

    /**
     * XML的BeanDefinitionReader
     */
    private val xmlReader = XmlBeanDefinitionReader(registry)

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator
        this.reader.setBeanNameGenerator(beanNameGenerator)
        this.scanner.setBeanNameGenerator(beanNameGenerator)
        this.xmlReader.setBeanNameGenerator(beanNameGenerator)
    }

    open fun setEnvironment(environment: ConfigurableEnvironment) {
        this.environment = environment
        this.reader.setEnvironment(environment)
        this.scanner.setEnvironment(environment)
        this.xmlReader.setEnvironment(environment)
    }

    open fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
        this.scanner.setResourceLoader(resourceLoader)
        this.xmlReader.setResourceLoader(resourceLoader)
    }


    /**
     * 根据各种资源的类型去匹配，执行BeanDefinition的加载
     */
    open fun load() {
        sources.forEach {
            when (it) {
                is Class<*> -> load(it)
                is CharSequence -> load(it)
                is Package -> load(it)
                is Resource -> load(it)
            }
        }
    }

    /**
     * 给定的source是一个字符串，我们尝试各种类型去进行加载
     *
     * @param resource resource
     */
    protected open fun load(resource: CharSequence) {
        val resolvedResource = this.scanner.getEnvironment().resolvePlaceholders(resource.toString())!!

        // 1.首先我们尝试把它当做一个类去进行解析
        try {
            load(ClassUtils.forName<Any?>(resolvedResource))
        } catch (ex: Exception) {
            // ignore and continue
        }

        // 2.尝试把它当做资源去进行加载
        if (loadAsResources(resolvedResource)) {
            return
        }

        // 3.把它当做一个JavaPackage去进行加载
        val pkg = findPackage(resolvedResource)
        if (pkg != null) {
            load(pkg)
        }
    }

    /**
     * 加载一个配置类，使用DefinitionReader去进行注册即可
     *
     * @param clazz 待注册的配置类
     */
    protected open fun load(clazz: Class<*>) = reader.registerBean(clazz)

    /**
     * 根据一个资源，去加载成为BeanDefinition
     *
     * @param resource Resource
     */
    protected open fun load(resource: Resource) {

    }

    /**
     * 将一个Package去加载成为BeanDefinition
     *
     * @param pkg 待加载的包
     */
    protected open fun load(pkg: Package) = this.scanner.doScan(pkg.name)


    /**
     * 将给定的资源去加载成为资源
     *
     * @param resolvedResource 资源路径
     * @return 如果有候选的可以被加载的，那么return true；否则return false
     */
    private fun loadAsResources(resolvedResource: String): Boolean {
        val resources = findResources(resolvedResource)
        var foundCandidate = false
        resources.filter(this::isLoadCandidate).forEach {
            load(it)
            foundCandidate = true
        }
        return foundCandidate
    }

    /**
     * 根据给定的路径去解析到资源列表
     *
     * @param resolvedResource 资源路径
     * @return 解析到的资源
     * @throws IllegalStateException 如果该资源路径无法被解析到的话
     */
    private fun findResources(resolvedResource: String): Array<Resource> {
        val resourceLoader = this.resourceLoader ?: PathMatchingResourcePatternResolver()
        try {
            if (resourceLoader is PathMatchingResourcePatternResolver) {
                return resourceLoader.getResources(resolvedResource)
            }
            return arrayOf(resourceLoader.getResource(resolvedResource))
        } catch (ex: IOException) {
            throw IllegalStateException("无法加载到指定的资源[$resolvedResource]")
        }
    }

    /**
     * 检查给定的资源是否是一个可以去加载的资源？
     *
     * @param resource 资源
     * @return 如果是可以加载的，那么return true；否则return false
     */
    private fun isLoadCandidate(resource: Resource?): Boolean {
        if (resource == null || !resource.exists()) {
            return false
        }
        if (resource is ClassPathResource) {
            val path = resource.getPath()
            if (path.indexOf(".") == -1) {
                return this::class.java.classLoader.getDefinedPackage(path) == null
            }
        }
        return true
    }

    /**
     * 将给定的资源当做一个包的方式去进行加载
     *
     * @param source source
     */
    private fun findPackage(source: String): Package? {
        return this::class.java.classLoader.getDefinedPackage(source)
    }
}