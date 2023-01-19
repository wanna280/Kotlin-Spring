package com.wanna.boot.web.servlet

import com.wanna.framework.core.Ordered
import com.wanna.common.logging.LoggerFactory
import javax.servlet.ServletContext

/**
 * Servlet3.0+的RegistrationBean的基础实现, 提供对于Servlet/Filter去注册到ServletContext当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
abstract class RegistrationBean : ServletContextInitializer, Ordered {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(RegistrationBean::class.java)
    }

    /**
     * Order
     */
    private var order: Int = Ordered.ORDER_LOWEST

    /**
     * enabled? 如果enabled=false, 将不会对它去进行注册
     */
    private var enabled: Boolean = true

    /**
     * 当前RegistrationBean所处的优先级
     *
     * @return order
     */
    override fun getOrder(): Int = this.order

    /**
     * 设置当前的RegistrationBean的优先级
     *
     * @param order order
     */
    open fun setOrder(order: Int) {
        this.order = order
    }

    override fun onStartup(servletContext: ServletContext) {
        val description = getDescription()
        if (!isEnabled()) {
            logger.info("$description was not registered(disabled, enabled=false)")
        }
        // register
        register(description, servletContext)
    }

    /**
     * 获取当前RegistrationBean的描述信息
     *
     * @return description
     */
    protected abstract fun getDescription(): String

    /**
     * 执行真正地去对当前的RegistrationBean去注册到ServletContext当中去
     *
     * @param description 当前RegistrationBean的描述信息
     * @param servletContext ServletContext
     */
    protected abstract fun register(description: String, servletContext: ServletContext)

    /**
     * 当前RegistrationBean是否启用?
     *
     * @return enabled
     */
    open fun isEnabled(): Boolean = this.enabled

    /**
     * 设置当前RegistrationBean的启用状态
     *
     * @param enabled enabled
     */
    open fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}