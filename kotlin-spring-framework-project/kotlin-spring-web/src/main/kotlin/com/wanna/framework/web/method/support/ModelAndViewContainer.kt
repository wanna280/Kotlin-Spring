package com.wanna.framework.web.method.support

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.ui.ModelMap

/**
 * ModelAndView的一个容器, 负责包装处理请求过程当中的视图与Model数据, 提供了一系列的对Model和View去进行操作的方法; 
 * 在处理完成请求之后, 会将全部的数据转移到ModelAndView当中, 交给视图解析器去进行视图的渲染; 
 *
 * @see ModelAndView
 */
open class ModelAndViewContainer {
    /**
     * 视图/视图名
     */
    @Nullable
    var view: Any? = null

    /**
     * 是否是重定向视图? 
     */
    var redirectModelScenario = false

    /**
     * model
     */
    var defaultModel = ModelMap()

    /**
     * 当前请求是否已经被处理过了(比如@ResponseBody的数据已经直接写出了, 就不需要使用ModelAndView了)
     */
    var requestHandled = false

    /**
     * ResponseStatus
     */
    @Nullable
    var responseStatus: Any? = null

    /**
     * 获取当前ModelAndViewContainer当中的Model数据
     */
    open fun getModel(): ModelMap = if (useDefaultModel()) defaultModel else ModelMap()

    /**
     * 添加Model数据到Model当中
     *
     * @param name 属性名
     * @param value 属性值
     */
    open fun addAttribute(name: String, @Nullable value: Any?): ModelAndViewContainer {
        getModel()[name] = value
        return this
    }

    /**
     * 往Model当中批量添加数据
     *
     * @param attributes 要添加的Model数据, Map
     */
    open fun addAttributes(attributes: Map<String, Any?>): ModelAndViewContainer {
        getModel().putAll(attributes)
        return this
    }

    /**
     * 从Model当中添加属性
     *
     * @param name model属性
     */
    open fun removeAttribute(name: String): ModelAndViewContainer {
        getModel().remove(name)
        return this
    }

    /**
     * 判断Model当中是否存在有该属性
     *
     * @param name model属性名
     * @return 如果Model当中存在, return true; 不存在则return false
     */
    open fun containsAttribute(name: String): Boolean = getModel().containsKey(name)

    /**
     * 是否要使用默认的Model? 
     *
     * @return 如果不是重定向, 则return true; 不然return false
     */
    open fun useDefaultModel(): Boolean = !redirectModelScenario
}