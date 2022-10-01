package com.wanna.framework.beans.propertyeditors

import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.beans.PropertyEditorSupport

/**
 * ClassEditor
 */
class ClassEditor(private val classLoader: ClassLoader?) : PropertyEditorSupport() {

    constructor() : this(null)

    override fun getAsText(): String {
        if (value == null) {
            return ""
        } else {
            return (value as Class<*>).name
        }
    }

    override fun setAsText(text: String?) {
        if (!StringUtils.hasText(text)) {
            value = null
        } else {
            value = ClassUtils.forName<Any>(text!!, classLoader)
        }
    }
}