package com.wanna.framework.context

import com.wanna.framework.context.aware.Aware

/**
 * 用于提供ApplicationContext的自动注入的Aware接口
 *
 * @see ApplicationContext
 * @see Aware
 */
fun interface ApplicationContextAware : Aware {

    /**
     * 自动注入[ApplicationContext]
     *
     * @param applicationContext ApplicationContext
     */
    fun setApplicationContext(applicationContext: ApplicationContext)
}