package com.wanna.boot.web.servlet

import javax.servlet.Filter

/**
 * Servlet3.0+的FilterRegistration Bean的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/12
 *
 * @param filter filter
 */
open class FilterRegistrationBean<T : Filter>(private val filter: T) : AbstractFilterRegistrationBean<T>() {

    override fun getFilter(): T = this.filter
}