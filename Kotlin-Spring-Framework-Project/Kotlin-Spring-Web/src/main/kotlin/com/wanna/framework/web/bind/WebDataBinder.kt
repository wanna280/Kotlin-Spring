package com.wanna.framework.web.bind

import com.wanna.framework.validation.DataBinder

/**
 * 它是一个DataBinder，提供了类型转换的支持以及数据的绑定功能
 *
 * @see DataBinder
 */
open class WebDataBinder(private val target: Any?, private val objectName: String) : DataBinder() {

    /**
     * 获取包装的目标对象
     *
     * @return 目标对象
     */
    open fun getTarget(): Any? = this.target

    /**
     * 获取目标对象的name
     *
     * @return objectName
     */
    open fun getObjectName() : String = this.objectName
}