package com.wanna.framework.web.method.support

import com.wanna.framework.web.ui.ModelMap

/**
 * ModelAndView的容器
 */
open class ModelAndViewContainer {

    // 视图/视图名
    var view: Any? = null

    // 是否是重定向视图？
    var redirectModelScenario = false

    // model
    var defaultModel = ModelMap()

    // 当前请求是否已经被处理过了(比如@ResponseBody的数据已经直接写出了，就不需要使用ModelAndView了)
    var requestHandled = false

    // ResponseStatus
    var responseStatus: Any? = null

    open fun getModel(): ModelMap {
        if (useDefaultModel()) {
            return defaultModel
        }
        return ModelMap()
    }

    open fun addAttribute(name: String, value: Any?) {
        getModel()[name] = value
    }

    open fun containsAttribute(name: String): Boolean {
        return getModel().containsKey(name)
    }

    open fun useDefaultModel(): Boolean = !redirectModelScenario
}