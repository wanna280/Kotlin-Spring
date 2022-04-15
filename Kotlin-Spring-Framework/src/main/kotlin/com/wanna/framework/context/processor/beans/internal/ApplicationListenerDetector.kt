package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.processor.beans.BeanPostProcessor

open class ApplicationListenerDetector(_applicationContext: ApplicationContext) : BeanPostProcessor {
    private val applicationContext: ApplicationContext = _applicationContext

}