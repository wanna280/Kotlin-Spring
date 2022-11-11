package com.wanna.framework.web.bind

import com.wanna.framework.validation.DataBinder

/**
 * 它是一个DataBinder，提供了类型转换的支持以及数据的绑定功能
 *
 * @see DataBinder
 */
open class WebDataBinder(target: Any?, objectName: String) : DataBinder(target, objectName) {


}