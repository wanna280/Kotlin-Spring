package com.wanna.framework.web.http

import java.io.Serializable

/**
 * 维护对于一个HttpCookie的信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/21
 * @param _name cookieName
 * @param _value cookieValue
 */
open class Cookie(_name: String, _value: String) : Cloneable, Serializable {

    /**
     * CookieName
     */
    val name = _name

    /**
     * CookieValue
     */
    val value = _value

    var comment: String? = null

    /**
     * Cookie的生效域名(起码得是二级域名才行)
     */
    var domain: String? = null

    /**
     * Cookie的最大有效存活时间(默认为-1, 表示一直存活)
     */
    var maxAge = -1

    /**
     * 当前Cookie是在哪个路径下生成的? 默认为"/"
     */
    var path: String = "/"

    /**
     * 如果设置了这个属性, 那么只有在SSH连接时才会传回该Cookie
     */
    var secure = false

    /**
     * Cookie版本, 存在有0/1/2三种版本(1已弃用);
     * 如果是0版本, 那么响应头为"Set-Cookie"; 如果是2版本, 那么响应头为"Set-Cookie2"
     */
    var version = 0

    /**
     * 如果设置为true, 那么JS脚本将会无法获取到这个字段的Cookie, 防止XSS攻击
     */
    var httpOnly = false

    override fun toString() = "Cookie(name='$name', value='$value')"
}