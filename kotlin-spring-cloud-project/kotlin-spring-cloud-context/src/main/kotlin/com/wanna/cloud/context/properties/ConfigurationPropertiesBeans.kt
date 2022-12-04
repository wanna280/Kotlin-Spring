package com.wanna.cloud.context.properties

import com.wanna.boot.context.properties.ConfigurationPropertiesBean
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.lang.Nullable

/**
 * 它维护了所有的RefreshScope内的ConfigurationPropertiesBean，将其维护起来，方便后期在环境信息发生改变时，可以去对RefreshScope内的Bean去进行刷新
 *
 * @see ConfigurationPropertiesRebinder
 */
@Component
open class ConfigurationPropertiesBeans : ApplicationContextAware, BeanFactoryAware, BeanPostProcessor {

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * BeanFactory
     */
    @Nullable
    private var beanFactory: ConfigurableListableBeanFactory? = null

    /**
     * Refresh是否已经初始化过了？第一次执行判断时，会完成RefreshScope的初始化工作...
     */
    private var refreshScopeInitialized = false

    /**
     * RefreshScope的scopeName
     */
    private var refreshScope: String? = null

    /**
     * 维护所有的ConfigurationPropertiesBean列表(key-beanName,value-ConfigurationPropertiesBean)
     */
    private val beans = LinkedHashMap<String, ConfigurationPropertiesBean>()

    /**
     * 设置ApplicationContext
     *
     * @param applicationContext ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        if (applicationContext is ConfigurableApplicationContext) {
            this.beanFactory = applicationContext.getBeanFactory()
        }
    }

    /**
     * 设置BeanFactory
     *
     * @param beanFactory beanFactory
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        if (beanFactory is ConfigurableListableBeanFactory) {
            this.beanFactory = beanFactory
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    open fun getApplicationContext(): ApplicationContext =
        this.applicationContext ?: throw IllegalStateException("ApplicationContext还未完成初始化, 不能去进行获取")

    /**
     * 在Bean初始化之前，对Bean检查，是否是一个ConfigurationPropertiesBean，如果是的话，那么需要去进行保存
     */
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        if (isRefreshScoped(beanName)) {
            val propertiesBean = ConfigurationPropertiesBean.get(getApplicationContext(), bean, beanName)
            if (propertiesBean != null) {
                this.beans[beanName] = propertiesBean
            }
        }
        return bean
    }

    /**
     * 判断该Bean是否在RefreshScope当中，比对beanName对应的BeanDefinition的scope是否是refresh
     *
     * @param beanName 要进行比对的beanName
     * @return 该Bean是否在RefreshScope当中？
     */
    private fun isRefreshScoped(beanName: String): Boolean {
        val beanFactory = this.beanFactory ?: return false  // not BeanFactory return false
        if (!this.refreshScopeInitialized && this.refreshScope == null) {
            this.refreshScopeInitialized = true
            beanFactory.getRegisteredScopeNames().forEach {
                if (beanFactory.getRegisteredScope(it) is com.wanna.cloud.context.scope.refresh.RefreshScope) {
                    this.refreshScope = it  // 初始化refreshScopeName
                }
            }
        }
        return this.refreshScope != null && beanFactory.containsBeanDefinition(beanName) &&
                beanFactory.getBeanDefinition(beanName).getScope() == this.refreshScope
    }

    /**
     * 获取维护的所有的ConfigurationPropertiesBean的beanName列表
     *
     * @return beanName列表
     */
    open fun getBeanNames(): Set<String> = LinkedHashSet(this.beans.keys)
}