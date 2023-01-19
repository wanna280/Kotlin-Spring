package com.wanna.boot.web.servlet

import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.LoggerFactory
import java.beans.Introspector
import javax.servlet.Registration
import javax.servlet.ServletContext

/**
 * Servlet3.0+的Dynamic Registration Bean的基础实现, 提供Servlet/Filter去注册到ServletContext当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @param D Servlet3.0+的API当中Filter/Servlet的Registration类型
 *
 * @see ServletRegistrationBean
 * @see FilterRegistrationBean
 * @see Registration.Dynamic
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
     * Servlet/Filter的初始化参数, 对于这些参数将会放入到Servlet3.0+ API的Registration当中...
     */
    private var initParameters = LinkedHashMap<String, String>()

    /**
     * RegistrationName
     *
     * @param name name
     */
    open fun setName(name: String) {
        this.name = name
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

    @Nullable
    protected abstract fun addRegistration(description: String, servletContext: ServletContext): D?

    /**
     * 对给定的Registration去执行自定义, 设置asyncSupported/initParameters
     *
     * @param registration Registration.Dynamic
     */
    protected open fun configure(registration: D) {
        registration.setAsyncSupported(asyncSupported)
        if (this.initParameters.isNotEmpty()) {
            registration.initParameters = initParameters
        }
    }

    /**
     * 当前的Registration是否支持去进行异步处理?
     *
     * @return 当前的Registration是否支持去进行异步处理?
     */
    open fun isAsyncSupported(): Boolean = this.asyncSupported

    /**
     * 设置当前的Registration是否支持去进行异步处理?
     *
     * @param asyncSupported asyncSupported
     */
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