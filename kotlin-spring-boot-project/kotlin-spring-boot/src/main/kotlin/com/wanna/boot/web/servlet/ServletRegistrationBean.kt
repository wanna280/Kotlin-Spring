package com.wanna.boot.web.servlet

import javax.servlet.Servlet
import javax.servlet.ServletContext
import javax.servlet.ServletRegistration

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class ServletRegistrationBean<T : Servlet>(val servlet: T) :
    DynamicRegistrationBean<ServletRegistration.Dynamic>() {

    companion object {
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

    override fun addRegistration(description: String, servletContext: ServletContext): ServletRegistration.Dynamic? {
        return servletContext.addServlet(getServletName(), servlet)
    }

    override fun configure(registration: ServletRegistration.Dynamic) {
        super.configure(registration)
        var urlMappings = urlMappings.toTypedArray()
        if (urlMappings.isEmpty() && alwaysMapUrl) {
            urlMappings = DEFAULT_MAPPINGS
        }
        registration.addMapping(*urlMappings)
    }


    /**
     * 获取ServletName
     *
     * @return servletName
     */
    open fun getServletName(): String = this.getOrDeduceName(this.servlet)
}