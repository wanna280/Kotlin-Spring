package com.wanna.framework.context.processor.beans.internal

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import java.util.Objects

open class ApplicationListenerDetector(_applicationContext: ApplicationContext) : BeanPostProcessor {
    private val applicationContext: ApplicationContext = _applicationContext

    /**
     * 重写equals方法，实现自定义的equals逻辑
     */
    override fun equals(other: Any?): Boolean =
        this === other || (other is ApplicationListenerDetector && other.applicationContext == this.applicationContext)

    override fun hashCode(): Int = Objects.hashCode(applicationContext)
}