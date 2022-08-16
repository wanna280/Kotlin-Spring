package com.wanna.framework.validation

import com.wanna.framework.beans.PropertyEditorRegistry
import com.wanna.framework.lang.Nullable

interface BindingResult : Errors {

    /**
     * 获取要去进行绑定的目标对象
     *
     * @return Bind Target
     */
    @Nullable
    fun getTarget(): Any?

    /**
     * 获取要去进行绑定的Model数据
     *
     * @return ModelMap
     */
    fun getModel(): MutableMap<String, Any>

    /**
     * 获取PropertyEditorRegistry
     *
     * @return PropertyEditorRegistry
     */
    fun getPropertyEditorRegistry(): PropertyEditorRegistry

    /**
     * 添加Error
     *
     * @param error error
     */
    fun addError(error: ObjectError)
}