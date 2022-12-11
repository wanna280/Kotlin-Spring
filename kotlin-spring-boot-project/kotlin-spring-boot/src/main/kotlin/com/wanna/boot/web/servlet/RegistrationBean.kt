package com.wanna.boot.web.servlet

import com.wanna.framework.core.Ordered
import org.slf4j.LoggerFactory
import javax.servlet.ServletContext

/**
 * Servlet3.0+的RegistrationBean的基础实现
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
     * enabled?
     */
    private var enabled: Boolean = true

    override fun getOrder(): Int = this.order

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

    protected abstract fun getDescription(): String

    protected abstract fun register(description: String, servletContext: ServletContext)

    open fun isEnabled(): Boolean = this.enabled

    open fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}