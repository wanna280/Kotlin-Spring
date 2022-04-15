package com.wanna.framework.context

import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

open class DefaultListableBeanFactory() : ConfigurableListableBeanFactory, BeanDefinitionRegistry,
    AbstractAutowireCapableBeanFactory() {

    // beanDefinitionMap
    private val beanDefinitionMap = HashMap<String, BeanDefinition>()

    // beanDefinitionNames
    private val beanDefinitionNames = HashSet<String>()

    // 这是一个依赖的比较器
    private var dependencyComparator: Comparator<Any?>? = null

    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        beanDefinitionNames += (name)
        beanDefinitionMap[name] = beanDefinition
    }

    override fun preInstantiateSingletons() {
        beanDefinitionNames.forEach { beanName ->
            var beanDefinition = getBeanDefinition(beanName)
            if (isFactoryBean(beanName)) {
                val bean = getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName)
                if (bean is FactoryBean<*>) {
                    var isEagerInit: Boolean = false
                    if (bean is SmartFactoryBean<*>) {
                        isEagerInit = bean.isEagerInit()
                    }
                    if (isEagerInit) {
                        getBean(beanName)
                    }
                }
            } else {
                getBean(beanName)
            }
        }

        // 在初始化完所有的单实例Bean之后，需要回调所有的SmartInitializingSingleton
        beanDefinitionNames.forEach {
            val singleton = getSingleton(it, false)
            if (singleton is SmartInitializingSingleton) {
                singleton.afterSingletonsInstantiated()
            }
        }
    }

    override fun isFactoryBean(beanName: String): Boolean {
        val transformBeanName = transformBeanName(beanName)
        val singleton = getSingleton(transformBeanName, false)
        if (singleton != null) {
            return singleton is FactoryBean<*>
        }
        return false
    }

    private fun transformBeanName(beanName: String): String {
        if (!beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            return beanName
        }
        // 切掉所有的FactoryBean的prefix，也就是&
        var name = beanName
        do {
            name = name.substring(BeanFactory.FACTORY_BEAN_PREFIX.length)
        } while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX))
        return name
    }

    override fun getBeanDefinition(beanName: String): BeanDefinition? {
        return beanDefinitionMap[beanName]
    }

    override fun getBeanDefinitionNames(): List<String> {
        return ArrayList(beanDefinitionNames)
    }

    override fun getBeanDefinitions(): List<BeanDefinition> {
        return ArrayList(beanDefinitionMap.values)
    }

    override fun getBeanDefinitionCounts(): Int {
        return beanDefinitionNames.size
    }

    override fun containsBeanDefinition(beanName: String): Boolean {
        return beanDefinitionMap[beanName] != null
    }

    open fun getDependencyComparator(): Comparator<Any?>? {
        return dependencyComparator
    }

    open fun setDependencyComparator(dependencyComparator: Comparator<Any?>) {
        this.dependencyComparator = dependencyComparator
    }
}