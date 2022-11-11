package com.wanna.cloud.client.loadbalancer

import com.wanna.framework.web.client.RestTemplate

/**
 * RestTemplate的自定义化器，实现这个接口的实现类，可以对RestTemplate去进行自定义
 *
 * @see RestTemplate
 */
interface RestTemplateCustomizer {
    fun customize(restTemplate: RestTemplate)
}