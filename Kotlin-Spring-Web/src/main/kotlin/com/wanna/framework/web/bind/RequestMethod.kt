package com.wanna.framework.web.bind

/**
 * 这是对HTTP请求方式的枚举
 */
enum class RequestMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    companion object {
        /**
         * 根据method的字符串，转为对应的RequestMethod枚举对象
         */
        @JvmStatic
        fun forName(method: String): RequestMethod {
            return valueOf(method.uppercase())
        }
    }

}