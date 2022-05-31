package com.wanna.framework.web.method.support

import com.wanna.framework.web.ui.ModelMap

/**
 * ModelAndView的容器
 */
class ModelAndViewContainer {

    // 视图/视图名
    var view: Any? = null

    // 是否是重定向视图？
    var redirectModelScenario = false

    // model
    var defaultModel = ModelMap()
}