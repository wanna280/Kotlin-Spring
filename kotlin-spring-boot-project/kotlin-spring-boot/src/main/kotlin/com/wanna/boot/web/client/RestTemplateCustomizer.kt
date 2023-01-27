package com.wanna.boot.web.client

import com.wanna.framework.web.client.RestTemplate

/**
 * [RestTemplate]的自定义化器, 实现这个接口的实现类, 可以对[RestTemplate]去进行自定义
 *
 * @see RestTemplate
 */
@FunctionalInterface
fun interface RestTemplateCustomizer {

    /**
     * 执行对于[RestTemplate]的自定义
     *
     * @param restTemplate RestTemplate
     */
    fun customize(restTemplate: RestTemplate)
}