package com.wanna.framework.web.method

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 默认的Request转换为ViewName的翻译器;
 * 它实现将request的url转换为viewName, 如果必要的话, 去掉前置的"/"、后置的"/", 以及文件的后缀名
 */
open class DefaultRequestToViewNameTranslator : RequestToViewNameTranslator {
    companion object {
        private const val SLASH = "/"
    }

    // viewName前缀, 需要给viewName拼接什么样的前缀? 
    var prefix = ""

    // viewName后缀, 需要给viewName拼接什么样的后缀? 
    var suffix = ""

    // viewName的分隔符
    var separator = SLASH

    // 是否要去掉前置的"/"(比如"/user")
    var stripLeadingSlash = true

    // 是否要去掉url后置的"/"(比如"/user/")
    var stripTrailingSlash = true

    // 是否要去掉文件后缀名? 
    var stripExtension = true

    override fun getViewName(request: HttpServerRequest): String? {
        var path = request.getUri()
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