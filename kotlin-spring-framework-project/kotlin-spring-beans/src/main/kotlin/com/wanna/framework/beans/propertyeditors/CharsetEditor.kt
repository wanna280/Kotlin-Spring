package com.wanna.framework.beans.propertyeditors

import com.wanna.framework.util.StringUtils
import java.beans.PropertyEditorSupport
import java.nio.charset.Charset

/**
 * 字符集的Editor, 支持去对字符集去进行类型的转换
 */
class CharsetEditor : PropertyEditorSupport() {
    override fun getAsText(): String {
        if (value == null) {
            return ""
        }
        return (value as Charset).name()
    }

    override fun setAsText(text: String?) {
        if (!StringUtils.hasText(text)) {
            value = null
        } else {
            value = Charset.forName(text)
        }
    }
}