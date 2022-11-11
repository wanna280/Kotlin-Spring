package com.wanna.framework.beans.propertyeditors

import com.wanna.framework.util.StringUtils
import java.beans.PropertyEditorSupport
import java.util.UUID

/**
 * UUID的Editor
 */
class UUIDEditor : PropertyEditorSupport() {
    override fun getAsText(): String {
        return value?.toString() ?: ""
    }

    override fun setAsText(text: String?) {
        if (StringUtils.hasText(text)) {
            value = UUID.fromString(text!!.trim())
        } else {
            value = null
        }
    }
}