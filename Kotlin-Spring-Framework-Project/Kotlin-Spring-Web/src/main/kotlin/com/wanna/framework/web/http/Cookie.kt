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
    val name = _name
    val value = _value
    var comment: String? = null
    var domain: String? = null
    var maxAge = -1
    var path: String? = null
    var secure = false
    var version = 0
}