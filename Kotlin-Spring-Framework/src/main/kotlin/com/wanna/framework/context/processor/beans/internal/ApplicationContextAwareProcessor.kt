package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.aware.ApplicationContextAware
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 这是一个ApplicationContext的处理器，注册负责完成一些Aware接口的回调
 */
class ApplicationContextAwareProcessor : BeanPostProcessor, ApplicationContextAware {

    var context: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }
}