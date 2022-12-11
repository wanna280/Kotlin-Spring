package com.wanna.boot.web.servlet

import com.wanna.framework.lang.Nullable
import javax.servlet.Filter
import javax.servlet.FilterRegistration
import javax.servlet.ServletContext

/**
 * Servlet3.0+的FilterRegistration的 Bean的抽象实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/12
 *
 * @see FilterRegistrationBean
 * @see DynamicRegistrationBean
 */
abstract class AbstractFilterRegistrationBean<T : Filter> :
    DynamicRegistrationBean<FilterRegistration.Dynamic>() {

    /**
     * 往ServletContext当中去注册当前RegistrationBean当中的Filter
     *
     * @param description description
     * @param servletContext ServletContext
     * @return FilterRegistration.Dynamic
     */
    @Nullable
    override fun addRegistration(description: String, servletContext: ServletContext): FilterRegistration.Dynamic? {
        val filter = getFilter()
        return servletContext.addFilter(getOrDeduceName(filter), filter)
    }

    /**
     * 获取Filter
     *
     * @return filter
     */
    abstract fun getFilter(): T

    /**
     * 获取Filter的描述信息
     *
     * @return description
     */
    override fun getDescription(): String = "filter ${getOrDeduceName(getFilter())}"
}