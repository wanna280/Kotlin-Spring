package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.ExposableEndpoint

/**
 * 支持去进行暴露的WebEndpoint
 */
interface ExposableWebEndpoint : ExposableEndpoint<WebOperation>