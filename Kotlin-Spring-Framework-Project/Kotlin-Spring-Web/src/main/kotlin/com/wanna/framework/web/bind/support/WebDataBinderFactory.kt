package com.wanna.framework.web.bind.support

import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * WebDataBinder的Factory，主要作用是去创建WebDataBinder，去提供数据的绑定功能，具体来说，其实，也是完成数据的类型转换功能
 *
 * @see WebDataBinder
 */
interface WebDataBinderFactory {
    /**
     * 创建WebDataBinder
     *
     * @param webRequest NativeWebRequest
     * @param target 要去进行绑定的JavaBean对象(比如User对象)
     * @param objectName 要去绑定的目标对象的name
     * @return 创建好的WebDataBinder
     */
    fun createBinder(webRequest: NativeWebRequest, target: Any?, objectName: String): WebDataBinder
}