package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.EndpointsSupplier

/**
 * Web环境下的可以去进行暴露的Endpoint的Supplier, 负责提供ExposableWebEndpoint的获取
 *
 * @see EndpointsSupplier
 * @see ExposableWebEndpoint
 */
interface WebEndpointsSupplier : EndpointsSupplier<ExposableWebEndpoint>