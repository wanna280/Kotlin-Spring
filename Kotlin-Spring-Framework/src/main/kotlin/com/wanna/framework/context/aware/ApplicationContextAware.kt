package com.wanna.framework.context.aware

import com.wanna.framework.context.ApplicationContext

/**
 * 设置ApplicationContext的Aware
 */
interface ApplicationContextAware :Aware {
    fun setApplicationContext(applicationContext: ApplicationContext)
}