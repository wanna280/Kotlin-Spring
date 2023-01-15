package com.wanna.framework.validation

import com.wanna.framework.beans.PropertyEditorRegistry
import com.wanna.framework.lang.Nullable

interface BindingResult : Errors {
    companion object {
        // BindingResult在放入Model当中时的前缀Key, 做标识作用
        @JvmStatic
        val MODEL_KEY_PREFIX = BindingResult::class.java.name + "."
    }

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