package com.wanna.framework.web.bind.support

import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.WebRequestDataBinder
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 默认的WebDataBinderFactory实现
 */
open class DefaultWebDataBinderFactory : WebDataBinderFactory {

    /**
     * 创建WebDataBinder，并完成Binder的初始化
     *
     * @param webRequest NativeWebRequest
     * @param target 要去进行创建绑定的目标JavaBean
     * @param objectName targetBean的name
     * @return 创建好的DataBinder
     */
    override fun createBinder(webRequest: NativeWebRequest, target: Any?, objectName: String): WebDataBinder {
        val dataBinder = createWebDataBinder(target, objectName)
        initBinder(dataBinder, webRequest)
        return dataBinder
    }

    /**
     * 创建WebDataBinder
     *
     * @param target 要去进行绑定的目标对象
     * @param objectName 要绑定的目标对象的name
     */
    open fun createWebDataBinder(target: Any?, objectName: String): WebDataBinder {
        return WebRequestDataBinder(target, objectName)
    }

    protected open fun initBinder(dataBinder: WebDataBinder, webRequest: NativeWebRequest) {

    }
}