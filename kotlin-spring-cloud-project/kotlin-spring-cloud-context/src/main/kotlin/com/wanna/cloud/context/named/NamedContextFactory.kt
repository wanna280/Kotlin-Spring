package com.wanna.cloud.context.named

import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.support.PropertySourcesPlaceholderConfigurer
import com.wanna.framework.core.environment.MapPropertySource
import java.util.concurrent.ConcurrentHashMap

/**
 * ## NamedContextFactory是什么? 
 *
 * * 1.它创建了一个Child Context的列表列表, 允许Specification在各自的Context当中去定义各自的Bean; 
 * * 2.在SpringCloud当中说, 它是从Spring-Cloud-Netflix当中的SpringClientFactory移植(ported from)过来的; 
 *
 * 在Spring当中, 它的子类实现有FeignContext(cloud-feign)、SpringClientFactory(cloud-ribbon)、LoadBalancerClientFactory
 * (cloud-balancer)这些情况; 需要注意的是, 这些不同的子类分别来自于SpringCloud的3个模块.(但是这三个子类的作者都是同样的...)
 *
 * ## Specification是什么? 
 *
 * * 1.Specification是维护了NamedContextFactory当中某个特定的childContext对应的的配置类列表, 可以配置要进行apply的childContext的name; 
 * * 2.如果name以"default."作为开头, 那么它会应用给所有的childContext当中; 如果不是以"default."作为开头, 那么将只会应用给指定的childContext.
 * * 3.如果该Specification符合当前childContext的name, 那么就会将其中的配置类列表, 全部apply到当前的childContext当中.
 *
 * @see NamedContextFactory.Specification
 * @param defaultConfigType 默认的配置类, 在创建每个childContext时, 都会去进行应用的默认配置类
 * @param propertySourceName 要往childContext当中添加的一个PropertySource的name
 * @param propertyName propertySource当中的propertyName(key)
 */
abstract class NamedContextFactory<C : NamedContextFactory.Specification>(
    private val defaultConfigType: Class<*>, private val propertySourceName: String, private val propertyName: String
) : DisposableBean, ApplicationContextAware {

    companion object {
        const val DEFAULT_PREFIX = "default."
    }

    // 维护了child ApplicationContext列表, key-childContextName, value-childApplicationContext
    private val contexts = ConcurrentHashMap<String, AnnotationConfigApplicationContext>()

    // 存放了大量的Specification的列表, key-childContextName, value-ConfigurationClasses of childContext
    private val configurations = ConcurrentHashMap<String, C>()

    // parent ApplicationContext
    private var parent: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.parent = applicationContext
    }

    /**
     * 设置NamedContextFactory的Configurations列表
     *
     * @param configurations Configurations
     */
    open fun setConfigurations(configurations: List<C>) {
        configurations.forEach { this.configurations[it.getName()] = it }
    }

    /**
     * 获取所有的ChildContext的name列表
     */
    open fun getContextNames(): Set<String> = HashSet(contexts.keys)

    /**
     * 给定具体的contextName, 去获取到对应的child ApplicationContext
     *
     * @param name childContextName
     * @return child ApplicationContext
     */
    protected open fun getContext(name: String): AnnotationConfigApplicationContext {
        if (!this.contexts.containsKey(name)) {
            synchronized(this.contexts) {
                if (!this.contexts.containsKey(name)) {
                    this.contexts[name] = createContext(name)
                }
            }
        }
        return this.contexts[name]!!
    }

    /**
     * 创建一个child ApplicationContext
     *
     * @param name childContext name
     * @return 创建好的并完成刷新的child ApplicationContext
     */
    protected open fun createContext(name: String): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()
        // 将该name对应的Specification当中的所有配置类注册到child Context当中
        if (this.configurations.containsKey(name)) {
            context.register(*this.configurations[name]!!.getConfigurations())
        }

        // 如果是default.作为起始的, 那么对于所有的Context都去进行apply...
        this.configurations.keys.forEach {
            if (it.startsWith(DEFAULT_PREFIX)) {
                context.register(*this.configurations[it]!!.getConfigurations())
            }
        }

        // 注册默认的配置类, 以及解析占位符的处理器...
        context.register(defaultConfigType, PropertySourcesPlaceholderConfigurer::class.java)

        // 设置parent
        if (this.parent != null) {
            context.setParent(this.parent!!)
            context.setBeanClassLoader(this.parent!!.getBeanClassLoader())
        }

        // 添加一个PropertySource...
        context.getEnvironment().getPropertySources()
            .addLast(MapPropertySource(propertySourceName, mapOf(propertyName to name)))

        context.refresh()  // refresh
        return context
    }

    /**
     * 给定childContextName和type, 去容器(包括childContext和parentContext)去找到合适的Bean
     *
     * @param name childContextName
     * @param type beanType
     * @return 从childContext(以及parentContext)当中获取到的Bean(如果没有找到的话, return null)
     */
    open fun <T : Any> getInstance(name: String, type: Class<T>): T? {
        val context = getContext(name)
        try {
            return context.getBean(type)
        } catch (ex: NoSuchBeanDefinitionException) {
            if (this.parent != null) {
                try {
                    return this.parent!!.getBean(type)
                } catch (ex: NoSuchBeanDefinitionException) {
                    // ignore
                }
            }
        }
        return null
    }

    /**
     * 给定childContextName和type, 去容器(包括childContext和parentContext)中找到合适的Bean的列表
     *
     * @param name childContextName
     * @param type beanType
     * @return 根据type去childContext(以及parentContext)当中找到的Bean的列表(Map<String,T>)
     */
    open fun <T : Any> getInstances(name: String, type: Class<T>): Map<String, T> {
        val context = getContext(name)
        val beans = HashMap(context.getBeansForType(type))
        if (this.parent != null) {  // merge parent
            beans += this.parent!!.getBeansForType(type)
        }
        return beans
    }

    override fun destroy() {

    }

    /**
     * Specification, 主要用来提供childName以及childContext的配置类的获取; 
     * 可以通过往容器当中去放入自定义Specification, 实现对childContext当中的配置类的自定义
     *
     * @see NamedContextFactory
     */
    interface Specification {
        fun getName(): String
        fun getConfigurations(): Array<Class<*>>
    }
}