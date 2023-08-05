package com.wanna.boot.actuate.endpoint.web

/**
 * 对于一个endpoint的链接的封装
 *
 * @param href 链接
 * @param templated 是否是一个模板参数? 对应的就是MVC当中的路径变量, 对于`@Selector`注解标注的参数, 将会转换成为模板参数
 */
data class Link(val href: String, val templated: Boolean = href.contains("{"))