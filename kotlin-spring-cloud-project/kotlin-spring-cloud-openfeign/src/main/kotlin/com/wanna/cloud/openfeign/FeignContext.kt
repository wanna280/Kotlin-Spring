package com.wanna.cloud.openfeign

import com.wanna.cloud.context.named.NamedContextFactory
import com.wanna.cloud.netflix.ribbon.SpringClientFactory

/**
 * FeignClient Context, 维护了一系列的childContext, 对应Ribbon的SpringClientFactory
 *
 * @see SpringClientFactory
 * @see NamedContextFactory
 */
open class FeignContext : NamedContextFactory<FeignClientSpecification>(
    FeignClientsConfiguration::class.java,
    "feign",
    "feign.client.name"
) {

}