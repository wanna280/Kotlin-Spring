package com.wanna.boot.web.servlet

import org.slf4j.LoggerFactory
import java.beans.Introspector
import javax.servlet.Registration
import javax.servlet.ServletContext

/**
 * Servlet3.0+的RegistrationBean的基础实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
abstract class DynamicRegistrationBean<D : Registration.Dynamic> : RegistrationBean() {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(RegistrationBean::class.java)
    }

    /**
     * async Supported
     */
    private var asyncSupported = true

    /**
     * name
     */
    private var name: String? = null

    /**
     * 初始化参数
     */
    private var initParameters = LinkedHashMap<String, String>()

    open fun setName(name: String) {
        this.name = name
    }

    override fun getDescription(): String {
        return ""
    }

    override fun register(description: String, servletContext: ServletContext) {
        // 添加一个Registration
        val registration = addRegistration(description, servletContext)
        if (registration == null) {
            logger.info("[$description] was not registered(possibly already registered?)")
            return
        }

        // 自定义Registration
        configure(registration)
    }

    protected abstract fun addRegistration(description: String, servletContext: ServletContext): D?

    protected open fun configure(registration: D) {
        registration.setAsyncSupported(asyncSupported)
        if (this.initParameters.isNotEmpty()) {
            registration.initParameters = initParameters
        }
    }


    open fun isAsyncSupported(): Boolean = this.asyncSupported

    open fun setAsyncSupported(asyncSupported: Boolean) {
        this.asyncSupported = asyncSupported
    }

    open fun setInitParameters(initParameters: Map<String, String>) {
        this.initParameters = LinkedHashMap(initParameters)
    }

    open fun getInitParameters(): Map<String, String> = this.initParameters

    /**
     * 获取或者是计算出来合适的name
     *
     * @param value value
     * @return name
     */
    protected open fun getOrDeduceName(value: Any): String {
        return name ?: Introspector.decapitalize(value::class.java.simpleName)
    }
}