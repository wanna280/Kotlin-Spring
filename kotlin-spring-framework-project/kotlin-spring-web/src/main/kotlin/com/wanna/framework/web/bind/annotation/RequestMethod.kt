package com.wanna.framework.web.bind.annotation

/**
 * 对各种HTTP请求方式的枚举
 */
enum class RequestMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    companion object {
        /**
         * 根据method的字符串, 转为对应的[RequestMethod]枚举对象
         *
         * @param method method methodStr
         * @return 解析得到的[RequestMethod]枚举值对象
         */
        @JvmStatic
        fun forName(method: String): RequestMethod {
            return valueOf(method.uppercase())
        }
    }
}