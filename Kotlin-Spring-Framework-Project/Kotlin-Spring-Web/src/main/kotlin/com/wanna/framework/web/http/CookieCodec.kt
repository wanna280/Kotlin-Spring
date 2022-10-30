package com.wanna.framework.web.http

/**
 * Cookie的编解码器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
interface CookieCodec {

    companion object {
        const val COOKIE_EXPIRES = "Expires"
        const val COOKIE_DOMAIN = "Domain"
        const val COOKIE_PATH = "Path"
        const val COOKIE_SECURE = "Secure"
        const val COOKIE_HTTP_ONLY = "HttpOnly"
        const val COOKIE_COMMENT = "Comment"
    }

    /**
     * 将Cookie去转换成为Header字符串
     *
     * @param cookies Cookies对象
     * @return 转换得到的CookieHeader字符串
     */
    fun encodeAsHeader(cookies: Array<Cookie>): String?

    /**
     * 根据Cookie的Header去转换成为Cookie对象
     *
     * @param cookies CookieHeader字符串
     * @return 转换得到的Cookies列表
     */
    fun decodeAsCookie(cookies: String): Array<Cookie>
}