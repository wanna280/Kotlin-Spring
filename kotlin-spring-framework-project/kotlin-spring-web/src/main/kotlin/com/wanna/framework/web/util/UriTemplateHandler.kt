package com.wanna.framework.web.util

import com.wanna.framework.web.client.RestTemplate
import java.net.URI

/**
 * 根据URI变量参数信息, 去扩展原始的URI Template
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see RestTemplate.uriTemplateHandler
 */
interface UriTemplateHandler {

    /**
     * 利用URI变量信息, 去扩展给定的[uriTemplate]
     *
     * @param uriTemplate uri模板
     * @param uriVariables Uri当中的变量参数
     * @return 扩展得到的URI对象
     */
    fun expand(uriTemplate: String, uriVariables: Map<String, *>): URI
}