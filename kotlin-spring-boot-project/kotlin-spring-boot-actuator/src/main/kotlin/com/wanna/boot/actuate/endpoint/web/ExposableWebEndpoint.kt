package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.ExposableEndpoint

/**
 * 支持去进行暴露的Web环境下的Endpoint(端点), 设置了Endpoint要去暴露的Operation的泛型O的具体类型为WebOperation
 *
 * @see ExposableEndpoint
 */
interface ExposableWebEndpoint : ExposableEndpoint<WebOperation>