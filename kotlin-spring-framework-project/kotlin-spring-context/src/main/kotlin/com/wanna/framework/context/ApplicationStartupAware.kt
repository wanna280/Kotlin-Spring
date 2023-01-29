package com.wanna.framework.context

import com.wanna.framework.beans.factory.Aware
import com.wanna.framework.core.metrics.ApplicationStartup

/**
 * 这是一个提供ApplicationStartup的注入的Aware接口
 *
 * @see ApplicationStartup
 */
interface ApplicationStartupAware : Aware {
    fun setApplicationStartup(applicationStartup: ApplicationStartup)
}