package com.wanna.framework.web.context

import com.wanna.framework.context.support.ApplicationObjectSupport

/**
 * Web环境下的ApplicationObjectSupport, 它提供了ApplicationContext的相关支持
 */
abstract class WebApplicationObjectSupport : ApplicationObjectSupport() {

    /**
     * ApplicationContext是否是必须的? 在WebApplication下, ApplicationContext是必须的
     */
    override fun isContextRequired(): Boolean {
        return true
    }
}