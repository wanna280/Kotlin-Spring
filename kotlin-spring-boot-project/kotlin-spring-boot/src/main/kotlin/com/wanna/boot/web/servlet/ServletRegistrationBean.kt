package com.wanna.boot.web.servlet

import com.wanna.framework.lang.Nullable
import javax.servlet.Servlet
import javax.servlet.ServletContext
import javax.servlet.ServletRegistration

/**
 * 针对于Servlet3.0+当中的Servlet的RegistrationBean的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 *
 * @param servlet Servlet
 * @param T  Servlet Type
 * @see ServletRegistration.Dynamic
 */
open class ServletRegistrationBean<T : Servlet>(private val servlet: T) :
    DynamicRegistrationBean<ServletRegistration.Dynamic>() {

    companion object {
        /**
         * 默认的Mapping
         */
        @JvmStatic
        private val DEFAULT_MAPPINGS = arrayOf("/*")
    }

    /**
     * UrlMappings
     */
    private var urlMappings = LinkedHashSet<String>()

    private var alwaysMapUrl = true

    open fun setUrlMappings(urlMappings: Collection<String>) {
        this.urlMappings = LinkedHashSet(urlMappings)
    }

    @Nullable
    override fun addRegistration(description: String, servletContext: ServletContext): ServletRegistration.Dynamic? {
        return servletContext.addServlet(getServletName(), servlet)
    }

    /**
     * 对于Servlet的RegistrationBean来说, 我们还需要去设置Registration的mapping
     *
     * @param registration ServletRegistration.Dynamic
     */
    override fun configure(registration: ServletRegistration.Dynamic) {
        super.configure(registration)
        var urlMappings = urlMappings.toTypedArray()
        if (urlMappings.isEmpty() && alwaysMapUrl) {
            urlMappings = DEFAULT_MAPPINGS
        }

        // 设置Servlet的urlMapping
        registration.addMapping(*urlMappings)
    }

    /**
     * 获取Servlet
     *
     * @return Servlet
     */
    open fun getServlet(): T = this.servlet

    /**
     * 获取当前Servlet RegistrationBean的描述信息
     *
     * @return description
     */
    override fun getDescription(): String = "servlet ${getServletName()}"

    /**
     * 获取ServletName
     *
     * @return servletName
     */
    open fun getServletName(): String = this.getOrDeduceName(this.servlet)
}