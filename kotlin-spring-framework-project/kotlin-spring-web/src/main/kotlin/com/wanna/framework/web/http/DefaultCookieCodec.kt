package com.wanna.framework.web.http

import com.wanna.framework.util.StringUtils

/**
 * 默认的Cookie编解码器的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/22
 */
open class DefaultCookieCodec : CookieCodec {

    companion object {
        /**
         * 对于多个Cookie的元素分隔符为"; "
         */
        const val COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI = "; "

        /**
         * Cookie的Key-Value的元素分隔符
         */
        const val COOKIE_HEADER_KEY_VALUE_SEPARATOR = "="
    }

    /**
     * 将给定的这些Cookie去转换成为Header字符串, 将会通过headerName="Set-Cookie"字段写回给浏览器
     *
     * @param cookies Cookies对象
     * @return 转换得到的CookieHeader字符串(如果不包含有Cookie的话, 那么return null)
     * @see HttpHeaders.SET_COOKIE
     */
    override fun encodeAsHeader(cookies: Array<Cookie>): String? {
        val cookieHeader = StringBuilder()
        cookies.forEach {
            cookieHeader.append(it.name).append(COOKIE_HEADER_KEY_VALUE_SEPARATOR).append(it.value).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            cookieHeader.append(CookieCodec.COOKIE_EXPIRES).append(COOKIE_HEADER_KEY_VALUE_SEPARATOR).append(it.maxAge).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI);
            cookieHeader.append(CookieCodec.COOKIE_PATH).append(COOKIE_HEADER_KEY_VALUE_SEPARATOR).append(it.path).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            if (it.domain != null) {
                cookieHeader.append(CookieCodec.COOKIE_DOMAIN).append(COOKIE_HEADER_KEY_VALUE_SEPARATOR).append(it.domain).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            }
            if (it.secure) {
                cookieHeader.append(CookieCodec.COOKIE_SECURE).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            }
            if (it.httpOnly) {
                cookieHeader.append(CookieCodec.COOKIE_HTTP_ONLY).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            }
            if (it.comment != null) {
                cookieHeader.append(CookieCodec.COOKIE_COMMENT).append(COOKIE_HEADER_KEY_VALUE_SEPARATOR).append(COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
            }
        }
        // 如果必要的话, 去掉末尾的"; "字符
        return if (cookieHeader.length > COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI.length) {
            cookieHeader.substring(0, cookieHeader.length - COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI.length)
        } else null
    }

    /**
     * 根据Cookie的Header(headerName="Cookie")去转换成为Cookie对象
     *
     * @param cookies CookieHeader字符串
     * @return 转换得到的Cookies列表
     * @see HttpHeaders.COOKIE
     */
    override fun decodeAsCookie(cookies: String): Array<Cookie> {
        val cookieElements = StringUtils.commaDelimitedListToStringArray(cookies, COOKIE_HEADER_ELEMENT_SEPARATOR_SEMI)
        val cookieList = ArrayList<Cookie>()
        for (cookie in cookieElements) {
            val split = cookie.split(COOKIE_HEADER_KEY_VALUE_SEPARATOR)
            if (split.size != 2) {
                continue
            }
            cookieList.add(Cookie(split[0], split[1]))
        }
        return cookieList.toTypedArray()
    }
}