package com.wanna.framework.web.method

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 默认的Request转换为ViewName的翻译器
 */
open class DefaultRequestToViewNameTranslator : RequestToViewNameTranslator {
    companion object {
        private const val SLASH = "/"
    }

    // viewName前缀
    var prefix = ""

    // viewName后缀
    var suffix = ""

    // viewName的分隔符
    var separator = SLASH

    // 是否要去掉前置的"/"
    var stripLeadingSlash = true

    // 是否要去掉后置的"/"
    var stripTrailingSlash = true

    // 是否要去掉文件后缀名？
    var stripExtension = true

    override fun getViewName(request: HttpServerRequest): String? {
        var path = request.getUrl()
        if (stripLeadingSlash && path.startsWith(SLASH)) {
            path = path.substring(1)
        }
        if (stripTrailingSlash && path.endsWith(SLASH)) {
            path = path.substring(0, path.length - 1)
        }
        if (stripExtension) {
            val index = path.lastIndexOf(".")
            if (index != -1) {
                path = path.substring(0, index)
            }
        }
        if (separator != SLASH) {
            path = path.replace(SLASH, separator)
        }
        return prefix + path + suffix
    }
}