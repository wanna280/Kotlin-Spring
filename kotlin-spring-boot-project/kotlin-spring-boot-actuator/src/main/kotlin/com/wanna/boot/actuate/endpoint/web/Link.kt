package com.wanna.boot.actuate.endpoint.web

/**
 * 对于一个endpoint的链接的封装
 */
class Link(val href: String, val templated: Boolean = href.contains("{")) {
    override fun toString() = "Link(href='$href', templated=$templated)"
}