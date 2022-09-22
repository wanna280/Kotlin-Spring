package com.wanna.boot.actuate.endpoint.web

/**
 * WebEndpoint的Response的封装，用于封装一层，将数据返回给客户端
 *
 * @param body ResponseBody
 * @param status HTTP响应状态码
 */
open class WebEndpointResponse<T>(var body: T?, var status: Int = 200) {

}