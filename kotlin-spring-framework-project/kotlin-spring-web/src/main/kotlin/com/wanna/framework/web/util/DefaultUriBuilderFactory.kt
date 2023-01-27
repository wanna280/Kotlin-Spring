package com.wanna.framework.web.util

import java.net.URI
import java.net.URLEncoder

/**
 * [UriTemplateHandler]的默认实现, TODO
 *
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see UriTemplateHandler
 * @see UriBuilderFactory
 */
open class DefaultUriBuilderFactory : UriBuilderFactory {

    override fun uriString(uriTemplate: String): UriBuilder = DefaultUriBuilder(uriTemplate)

    override fun builder(): UriBuilder = DefaultUriBuilder("")

    override fun expand(uriTemplate: String, uriVariables: Map<String, *>): URI =
        uriString(uriTemplate).build(uriVariables)

    /**
     * [UriBuilder]的默认实现
     *
     * @param uriTemplate URI模板
     */
    private class DefaultUriBuilder(private val uriTemplate: String) : UriBuilder {
        override fun build(uriVariables: Map<String, *>): URI {
            val builder = StringBuilder()
            if (uriVariables.isNotEmpty()) {
                builder.append("?")
            }
            uriVariables.forEach { (name, value) -> builder.append(name).append("=").append(value).append("&") }

            val uriStr = uriTemplate + if (builder.isNotEmpty()) URLEncoder.encode(
                builder.substring(0, builder.length - 1), "UTF-8"
            ) else ""

            return URI(uriStr)
        }
    }
}