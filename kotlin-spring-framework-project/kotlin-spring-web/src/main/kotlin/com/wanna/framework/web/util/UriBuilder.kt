package com.wanna.framework.web.util

import java.net.URI

/**
 * [URI]的Builder
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 */
interface UriBuilder {

    /**
     * 根据[uriVariables]去构建URI
     *
     * @param uriVariables URI变量信息
     * @return URI
     */
    fun build(uriVariables: Map<String, *>): URI
}