package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.ExposableEndpoint

/**
 * endpoint的链接解析器, 负责将所有的endpoint, 去转换为url
 *
 * @see Link
 */
open class EndpointLinksResolver(val endpoints: Collection<ExposableEndpoint<*>>) {

    open fun resolveLinks(requestUrl: String): Map<String, Link> {
        val links = LinkedHashMap<String, Link>()
        links["self"] = Link(requestUrl)
        endpoints.forEach {
            it.getOperations().forEach { operation ->
                if (operation is WebOperation) {
                    links[operation.getId()] = Link(requestUrl + "/" + operation.getRequestPredicate().getPath())
                }
            }
        }
        return links
    }
}