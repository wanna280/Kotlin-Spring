package com.wanna.framework.web.bind

import com.wanna.framework.beans.BeanWrapperImpl
import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于Web的Request去进行数据绑定
 */
open class WebRequestDataBinder(target: Any?, objectName: String) : WebDataBinder(target, objectName) {
    open fun bind(request: NativeWebRequest) {
        val serverRequest = request.getNativeRequest(HttpServerRequest::class.java)

        // 将ParamMap转换为MutablePropertyValues
        val mutablePropertyValues = MutablePropertyValues(serverRequest.getParamMap())
        val beanWrapper = BeanWrapperImpl(getTarget())
        beanWrapper.setConversionService(getConversionService())
        // 设置PropertyValues
        beanWrapper.setPropertyValues(mutablePropertyValues)
    }
}