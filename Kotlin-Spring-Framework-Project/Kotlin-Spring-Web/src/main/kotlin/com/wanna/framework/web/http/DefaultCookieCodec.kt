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
        const val COMMA = "; "
        const val EQUALS = "="
    }

    /**
     * 将Cookie去转换成为Header字符串
     *
     * @param cookies Cookies对象
     * @return 转换得到的CookieHeader字符串
     */
    override fun encodeAsHeader(cookies: Array<Cookie>): String {
        val cookieHeader = StringBuilder()
        cookies.forEach {
            cookieHeader.append(it.name).append(EQUALS).append(it.value).append(COMMA)
            cookieHeader.append(CookieCodec.COOKIE_EXPIRES).append(EQUALS).append(it.maxAge).append(COMMA);
            cookieHeader.append(CookieCodec.COOKIE_PATH).append(EQUALS).append(it.path).append(COMMA)
            if (it.domain != null) {
                cookieHeader.append(CookieCodec.COOKIE_DOMAIN).append(EQUALS).append(it.domain).append(COMMA)
            }
            if (it.secure) {
                cookieHeader.append(CookieCodec.COOKIE_SECURE).append(COMMA)
            }
            if (it.httpOnly) {
                cookieHeader.append(CookieCodec.COOKIE_HTTP_ONLY).append(COMMA)
            }
            if (it.comment != null) {
                cookieHeader.append(CookieCodec.COOKIE_COMMENT).append(EQUALS).append(COMMA)
            }
        }
        return cookieHeader.substring(0, cookieHeader.length - COMMA.length)
    }

    /**
     * 根据Cookie的Header去转换成为Cookie对象
     *
     * @param cookies CookieHeader字符串
     * @return 转换得到的Cookies列表
     */
    override fun decodeAsCookie(cookies: String): Array<Cookie> {
        val cookieElements = StringUtils.commaDelimitedListToStringArray(cookies, COMMA)
        val cookieList = ArrayList<Cookie>()
        for (cookie in cookieElements) {
            val split = cookie.split(EQUALS)
            if (split.size != 2) {
                continue
            }
            cookieList.add(Cookie(split[0], split[1]))
        }
        return cookieList.toTypedArray()
    }
}