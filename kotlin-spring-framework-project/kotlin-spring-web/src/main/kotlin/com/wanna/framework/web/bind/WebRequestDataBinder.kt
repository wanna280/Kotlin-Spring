package com.wanna.framework.web.bind

import com.wanna.framework.beans.BeanWrapperImpl
import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于Web的Request去进行数据绑定
 *
 * @param target 要去进行绑定的目标对象
 * @param objectName objectName
 */
open class WebRequestDataBinder(target: Any?, objectName: String) : WebDataBinder(target, objectName) {

    /**
     * 根据给定的[NativeWebRequest], 去对目标对象去进行绑定
     *
     * @param request request
     */
    open fun bind(request: NativeWebRequest) {
        if (getTarget() == null) {
            return
        }
        val serverRequest = request.getNativeRequest(HttpServerRequest::class.java)

        // 将ParamMap转换为MutablePropertyValues
        val mutablePropertyValues = MutablePropertyValues(serverRequest.getParamMap())
        val beanWrapper = BeanWrapperImpl(getTarget() ?: throw IllegalStateException("target cannot be null"))
        beanWrapper.setConversionService(getConversionService())
        // 设置PropertyValues
        beanWrapper.setPropertyValues(mutablePropertyValues)
    }
}