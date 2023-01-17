package com.wanna.cloud.context.scope.refresh

import com.wanna.cloud.context.scope.GenericScope
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.ContextRefreshedEvent
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.Ordered

/**
 * 这是一个RefreshScope, 它的父类是GenericScope, 是一个BeanFactoryPostProcessor, 会自己将自己注册到BeanFactory的Scope当中
 *
 * @see postProcessBeanFactory
 */
@Component
open class RefreshScope : GenericScope(), Ordered, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    init {
        super.setName("refresh") // set scopeName
    }

    private var order: Int = Ordered.ORDER_LOWEST - 10

    private lateinit var applicationContext: ApplicationContext

    private lateinit var registry: BeanDefinitionRegistry

    // 是否渴望去进行初始化? 如果渴望去进行初始化的话, 那么容器启动完成就去实例化所有的Bean
    private var eager: Boolean = true

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        super.postProcessBeanDefinitionRegistry(registry)
        this.registry = registry
    }

    /***
     * 监听容器已经启动好的事件, 如果当前Scope内渴望被加载的话, 那么Scope内的Bean在启动时, 就会去完成初始化;
     * 因为对于自定义Scope的情况, 默认情况下是不会去进行Bean的实例化和初始化操作的...
     *
     * @param event ContextRefreshedEvent
     */
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val registry = this.registry
        val applicationContext = this.applicationContext
        if (this.eager && event.applicationContext == applicationContext) {
            registry.getBeanDefinitionNames().forEach {
                if (registry.getBeanDefinition(it).getScope() == this.getName()) {
                    applicationContext.getBean(it)?.javaClass
                }
            }
        }
    }

    /**
     * 给定一个beanName, 去进行refresh
     *
     * @param name beanName
     */
    open fun refresh(name: String): Boolean {
        if (super.destroy(name)) {
            this.applicationContext.publishEvent(RefreshScopeRefreshedEvent(name))
            return true
        }
        return false
    }

    /**
     * 刷新Scope内的全部Bean, 在刷新完成时, 发布事件RefreshScopeRefreshedEvent
     */
    open fun refreshAll() {
        super.destroy()  // super.destroy, 摧毁RefreshScope内的全部Bean
        this.applicationContext.publishEvent(RefreshScopeRefreshedEvent())
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun getOrder(): Int {
        return this.order
    }

    open fun setOrder(order: Int) {
        this.order = order
    }
}