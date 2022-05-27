package com.wanna.framework.beans.propertyeditors

import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils
import java.beans.PropertyEditorSupport

/**
 * ClassArray的Editor
 */
class ClassArrayEditor : PropertyEditorSupport() {
    @Suppress("UNCHECKED_CAST")
    override fun getAsText(): String {
        if (value == null) {
            return ""
        }
        return CLASS_ARRAY_TYPE.cast(value).joinToString(",") { it.name }
    }

    override fun setAsText(text: String?) {
        if (!StringUtils.hasText(text)) {
            value = null
        } else {
            value = StringUtils.commaDelimitedListToStringArray(text).map { ClassUtils.forName<Any>(it) }.toTypedArray()
        }
    }
}