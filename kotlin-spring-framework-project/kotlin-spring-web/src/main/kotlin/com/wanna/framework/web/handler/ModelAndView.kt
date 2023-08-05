package com.wanna.framework.web.handler

import com.wanna.framework.web.ui.ModelMap
import com.wanna.framework.web.ui.View

/**
 * 模型和视图
 */
class ModelAndView {

    // 视图/视图名
    var view: Any? = null

    // modelMap
    var modelMap: ModelMap? = null

    // http status
    var status: Int = 200

    private var cleared: Boolean = false

    fun setView(view: View) {
        this.view = view
    }

    fun setViewName(viewName: String) {
        this.view = viewName
    }

    /**
     * 获取视图对象
     */
    fun getView(): View? = if (view is View) (this.view as View) else null

    /**
     * 获取视图名
     */
    fun getViewName(): String? = if (view is String) (view as String) else null

    fun hasView(): Boolean = this.view != null

    /**
     * 当前ModelAndView是否为空
     *
     * @return 当view和viewName都为空时, return true; 不然return false
     */
    fun isEmpty(): Boolean = this.view == null && this.modelMap == null

    /**
     * 存在的视图是否是一个视图引用? 
     *
     * @return 如果是String说明是视图引用, 不然说明不是
     */
    fun isReference(): Boolean = this.view is String

    /**
     * 清空ModelAndView当中的所有的数据
     */
    fun clear() {
        this.view = null
        this.modelMap = null
        this.cleared = true
    }
}