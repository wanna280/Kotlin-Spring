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

    open fun getModel(): ModelMap {
        if (useDefaultModel()) {
            return defaultModel
        }
        return ModelMap()
    }

    open fun useDefaultModel(): Boolean = !redirectModelScenario
}