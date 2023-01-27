package com.wanna.boot.autoconfigure.web.client

import com.wanna.boot.autoconfigure.http.HttpMessageConverters
import com.wanna.boot.web.client.RestTemplateBuilder
import com.wanna.boot.web.client.RestTemplateCustomizer
import com.wanna.boot.web.client.RestTemplateRequestCustomizer
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.client.RestTemplate

/**
 * [RestTemplateBuilder]的配置器, 负责将组合的这些配置信息, 去应用给[RestTemplateBuilder]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see RestTemplateBuilder
 * @see RestTemplate
 */
class RestTemplateBuilderConfigurer {

    /**
     * MessageConverters
     */
    @Nullable
    var httpMessageConverters: HttpMessageConverters? = null

    /**
     * 对于[RestTemplate]去进行自定义的自定义化器列表
     */
    @Nullable
    var restTemplateCustomizers: List<RestTemplateCustomizer>? = null

    /**
     * 对[RestTemplate]的Request去进行自定义的自定义器列表
     */
    @Nullable
    var restTemplateRequestCustomizers: List<RestTemplateRequestCustomizer<*>>? = null


    /**
     * 对于[RestTemplateBuilder]去进行配置, 将相关的配置信息, 全部merge到[RestTemplateBuilder]当中去
     *
     * @param builder builder
     * @return configured builder
     */
    fun configure(builder: RestTemplateBuilder): RestTemplateBuilder {
        var builderToUse = builder
        if (httpMessageConverters != null) {
            builderToUse = builder.messageConverters(httpMessageConverters!!.getConverters())
        }
        if (!restTemplateCustomizers.isNullOrEmpty()) {
            builderToUse = builder.customizers(restTemplateCustomizers!!)
        }

        if (!restTemplateRequestCustomizers.isNullOrEmpty()) {
            builderToUse = builder.requestCustomizers(this.restTemplateRequestCustomizers!!)
        }
        return builderToUse
    }
}