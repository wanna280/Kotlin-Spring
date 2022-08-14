package com.wanna.framework.web.http.converter

/**
 * 找不到合适的HttpMessageConverter去进行写出的异常
 *
 * @param msg message
 */
class HttpMessageNotWritableException(msg: String) : RuntimeException(msg)