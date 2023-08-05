package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.support.DefaultWebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.InvocableHandlerMethod

/**
 * 这是一个新增了支持使用@InitBinder方法去对WebDataBinder进行初始化的DataBinderFactory
 *
 * @param binderMethods @InitBinder的HandlerMethod
 */
open class InitBinderDataBinderFactory(private val binderMethods: List<InvocableHandlerMethod>) :
    DefaultWebDataBinderFactory() {

    /**
     * 初始化Binder, 遍历所有的@InitBinder方法, 交给它对WebDataBinder去进行初始化工作;
     *
     * @param dataBinder 目标Binder
     * @param webRequest webRequest
     */
    override fun initBinder(dataBinder: WebDataBinder, webRequest: NativeWebRequest) {
        // apply所有的@InitBinder方法
        // 并且给定了DataBinder, 也就是说, 一个可以去注入WebDataBinder, 完成自定义操作
        binderMethods.forEach { it.invokeForRequest(webRequest, null, dataBinder) }
    }
}