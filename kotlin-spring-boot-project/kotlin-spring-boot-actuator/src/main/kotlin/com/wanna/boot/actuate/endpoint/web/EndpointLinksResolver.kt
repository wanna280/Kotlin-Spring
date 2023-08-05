package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.ExposableEndpoint

/**
 * endpoint的链接解析器, 负责将所有的endpoint, 去转换为url
 *
 * @see Link
 *
 * @param endpoints 要去进行暴露的Endpoint列表
 */
open class EndpointLinksResolver(private val endpoints: Collection<ExposableEndpoint<*>>) {

    /**
     * 解析Endpoint的对应链接
     *
     * @param requestUrl requestUrl
     * @return 各个Endpoint的相关链接的映射关系
     */
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