package com.wanna.boot.context

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.StringUtils
import java.util.concurrent.atomic.AtomicLong

/**
 * 为了去设置[ApplicationContext]的ContextId的[ApplicationContextInitializer];
 * 如果没有设置"spring.application.name"这个属性的话, 那么会将"application"去设置为默认的contextId
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/12
 *
 * @see APPLICATION_ID_PROPERTY_NAME
 * @see DEFAULT_APPLICATION_ID
 */
open class ContextIdApplicationContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {

    companion object {
        /**
         * 配置ApplicationId的属性名
         */
        private const val APPLICATION_ID_PROPERTY_NAME = "spring.application.name"

        /**
         * 默认的ApplicationId, 为"application"
         */
        private const val DEFAULT_APPLICATION_ID = "application"
    }

    /**
     * Order
     */
    private var order = Ordered.ORDER_LOWEST - 10

    /**
     * 初始化[ApplicationContext], 将ApplicationContext的Id去完成初始化
     *
     * @param applicationContext ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val contextId = getContextId(applicationContext)
        applicationContext.setId(contextId.id)

        // 将ContextId对象放在BeanFactory当中, 方便child ApplicationContext可以获取到并使用
        applicationContext.getBeanFactory().registerSingleton(ContextId::class.java.name, contextId)
    }


    /**
     * 为给定的[ApplicationContext]去生成ContextId
     *
     * * 1.如果parent当中已经有[ContextId]的话, 那么使用它去生成child ContextId
     * * 2.如果parent当中没有[ContextId]的话, 新创建一个ContextId去进行返回
     *
     * @param applicationContext ApplicationContext
     * @return 为该ApplicationContext生成的contextId
     */
    private fun getContextId(applicationContext: ConfigurableApplicationContext): ContextId {
        val parent = applicationContext.getParent()

        // 如果parent当中有ContextId, 那么使用它去创建child ContextId
        if (parent != null && parent.containsBean(ContextId::class.java.name)) {
            return parent.getBean(ContextId::class.java).createChild()
        }

        // 如果parent当中没有ContextId, 那么从Environment当中去获取一个合适的ContextId
        return ContextId(getApplicationId(applicationContext.getEnvironment()))
    }

    /**
     * 从[Environment]当中去获取到合适的ApplicationId
     *
     * @param environment Environment
     * @return ApplicationId
     */
    private fun getApplicationId(environment: Environment): String {
        val contextId = environment.getProperty(APPLICATION_ID_PROPERTY_NAME)
        if (StringUtils.hasText(contextId)) {
            return contextId!!
        }
        return DEFAULT_APPLICATION_ID
    }

    override fun getOrder(): Int = this.order

    open fun setOrder(order: Int) {
        this.order = order
    }

    /**
     * ContextId
     *
     * @param id 要使用的ApplicationContext Id
     */
    class ContextId(val id: String) {
        private val children = AtomicLong()

        /**
         * 为child context去生成child context id
         *
         * @return child context id
         */
        fun createChild(): ContextId = ContextId(id + "-" + children.getAndIncrement())
    }
}