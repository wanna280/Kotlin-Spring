package com.wanna.framework.context

import com.wanna.framework.context.aware.Aware

/**
 * 设置ApplicationContext的Aware
 */
interface ApplicationContextAware : Aware {
    fun setApplicationContext(applicationContext: ApplicationContext)
}