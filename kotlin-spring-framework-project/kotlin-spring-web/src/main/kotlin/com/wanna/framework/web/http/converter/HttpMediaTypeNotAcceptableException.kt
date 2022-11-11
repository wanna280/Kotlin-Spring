package com.wanna.framework.web.http.converter


/**
 * 发生这个异常时，代表着服务端无法生成一个可以让客户端接收的数据
 *
 * @param msg message
 */
class HttpMediaTypeNotAcceptableException(msg: String) : RuntimeException(msg)