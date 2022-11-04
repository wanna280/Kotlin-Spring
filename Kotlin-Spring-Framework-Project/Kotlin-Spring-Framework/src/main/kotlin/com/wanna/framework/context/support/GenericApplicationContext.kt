package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.metrics.ApplicationStartup
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 这是一个通用的ApplicationContext，它组合了BeanFactory，为AbstractApplicationContext当中的相关方法提供了实现；
 * 子类当中只要继续根据此类去扩展自己相关的功能(比如注册配置类)，即可实现出一个比较完整的的ApplicationContext
 *
 * @see AbstractApplicationContext
 *
 * @param beanFactory 内部需要去组合DefaultListableBeanFactory
 */
abstract class GenericApplicationContext(private val beanFactory: DefaultListableBeanFactory) :
    AbstractApplicationContext(), BeanDefinitionRegistry {

    /**
     * 提供一个无参数的副构造器，创建一个默认的BeanFactory
     */
    constructor() : this(DefaultListableBeanFactory())

    /**
     * 容器是否已经刷新过的标志位？容器不允许被重复刷新
     */
    private val refreshed = AtomicBoolean(false)

    /**
     * 是否自定义过了ClassLoader？
     */
    private var customClassLoader = false

    /**
     * ResourceLoader，支持去进行自定义ResourceLoader；
     * AbstractApplicationContext当中，本身就已经支持了ResourceLoader；
     * 这里支持你去进行自定义，如果你自定义了，那么将会使用你给定的作为ResourceLoader；
     * 如果你没有去进行自定义，那么将会使用AbstractApplicationContext作为ResourceLoader
     *
     * @see AbstractApplicationContext
     * @see ResourceLoader
     */
    private var resourceLoader: ResourceLoader? = null

    /**
     * 刷新BeanFactory(不允许重复刷新)
     *
     * @throws IllegalStateException 如果BeanFactory被重复刷新
     */
    @Throws(IllegalStateException::class)
    override fun refreshBeanFactory() {
        if (!refreshed.compareAndSet(false, true)) {
            throw IllegalStateException("Spring BeanFactory不能被重复进行刷新")
        }
    }

    /**
     * 设置ApplicationStartup，同时将ApplicationStartup设置到BeanFactory当中
     *
     * @param applicationStartup ApplicationStartup
     */
    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        super.setApplicationStartup(applicationStartup)
        this.beanFactory.setApplicationStartup(applicationStartup) // 给beanFactory也设置上ApplicationStartup
    }

    /**
     * 获取ApplicationContext内部组合的BeanFactory
     *
     * @return BeanFactory
     */
    override fun getBeanFactory(): ConfigurableListableBeanFactory = beanFactory

    /**
     * 获取当前[GenericApplicationContext]内部的[DefaultListableBeanFactory]
     *
     * @return 内部的DefaultListableBeanFactory
     */
    fun getDefaultListableBeanFactory(): DefaultListableBeanFactory = this.beanFactory


    /**
     * [ApplicationContext]支持去获取[AutowireCapableBeanFactory]，去提供Bean的创建和初始化工作
     *
     * @return AutowireCapableBeanFactory
     */
    override fun getAutowireCapableBeanFactory(): AutowireCapableBeanFactory = beanFactory

    /**
     * 是否允许发生循环依赖？
     *
     * @return 如果允许循环依赖，那么return true；否则return false
     */
    open fun isAllowCircularReferences() = beanFactory.isAllowCircularReferences()

    /**
     * 获取所有的已经注册的BeanDefinition的beanName列表
     *
     * @return 当前BeanFactory当中已经注册的BeanDefinition的beanName列表
     */
    override fun getBeanDefinitionNames() = beanFactory.getBeanDefinitionNames()

    /**
     * 获取已经注册的BeanDefinition的数量
     *
     * @return BeanFactory当中已经注册的BeanDefinition的数量
     */
    override fun getBeanDefinitions() = beanFactory.getBeanDefinitions()

    /**
     * 根据beanName去获取到BeanDefinition；
     * 如果想要不抛出异常，请先使用[containsBeanDefinition]方法去进行判断该BeanDefinition是否存在
     *
     * @param beanName beanName
     * @return BeanDefinition
     * @throws NoSuchBeanDefinitionException 如果不存在这样的一个BeanName对应的BeanDefinition的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    override fun getBeanDefinition(beanName: String) = beanFactory.getBeanDefinition(beanName)

    /**
     * 检查BeanFactory当中是否包含了给定的beanName的BeanDefinition
     *
     * @param name beanName
     * @return 如果包含了该BeanDefinition，那么return true；否则return false
     */
    override fun containsBeanDefinition(name: String) = beanFactory.containsBeanDefinition(name)

    /**
     * 获取当前BeanFactory当中的BeanDefinition的数量
     *
     * @return BeanFactory当中的BeanDefinition的数量
     */
    override fun getBeanDefinitionCount() = beanFactory.getBeanDefinitionCount()

    /**
     * 根据beanName去移除一个指定的BeanDefinition
     *
     * @param name 需要移除BeanDefinition的beanName
     */
    override fun removeBeanDefinition(name: String) = beanFactory.removeBeanDefinition(name)

    /**
     * 注册一个BeanDefinition到当前的BeanFactory当中来
     *
     * @param name beanName
     * @param beanDefinition 需要去进行注册的BeanDefinition
     */
    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) =
        beanFactory.registerBeanDefinition(name, beanDefinition)

    /**
     * 设置parentApplicationContext的同时，需要设置parentBeanFactory
     *
     * @param parent parentApplicationContext
     */
    override fun setParent(parent: ApplicationContext?) {
        super.setParent(parent)
        this.beanFactory.setParentBeanFactory(getInternalParentBeanFactory())
    }

    /**
     * 获取ApplicationContext内部的BeanFactory；
     * * 1.如果ApplicationContext是ConfigurableApplicationContext，那么可以从它里面获取BeanFactory；
     * * 2.如果ApplicationContext不是ConfigurableApplicationContext，那么fallback直接就使用它作为parentBeanFactory；
     */
    protected open fun getInternalParentBeanFactory(): BeanFactory? {
        val parent = getParent()
        return if (parent is ConfigurableApplicationContext) parent.getBeanFactory() else parent
    }

    open fun setAllowCircularReferences(allowCircularReferences: Boolean) =
        beanFactory.setAllowCircularReferences(allowCircularReferences)

    override fun setBeanClassLoader(beanClassLoader: ClassLoader) =
        this.beanFactory.setBeanClassLoader(beanClassLoader)

    override fun getBeanClassLoader() = this.beanFactory.getBeanClassLoader()

    /**
     * closeBeanFactory, 已经没有别的操作可以做了，所有的摧毁操作都在之前就已经被做完了
     */
    override fun closeBeanFactory() {

    }

    /**
     * 设置ResourceLoader
     *
     * @param resourceLoader ResourceLoader
     */
    open fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    /**
     * 我们需要用户是否有自定义过ClassLoader，因此重写这个方法
     */
    override fun setClassLoader(classLoader: ClassLoader?) {
        super.setClassLoader(classLoader)
        this.customClassLoader = true
    }

    override fun getResource(location: String): Resource {
        if (this.resourceLoader != null) {
            return this.resourceLoader?.getResource(location) ?: throw IllegalStateException("ResourceLoader不能为空")
        }
        return super.getResource(location)
    }

    override fun getClassLoader(): ClassLoader? {
        // 只有在没有自定义ClassLoader的情况下，才使用ResourceLoader的ClassLoader
        // 在自定义了的情况下，应该invoke super
        if (this.resourceLoader != null && !customClassLoader) {
            return this.resourceLoader?.getClassLoader() ?: throw IllegalStateException("ResourceLoader不能为空")
        }
        return super.getClassLoader()
    }
}