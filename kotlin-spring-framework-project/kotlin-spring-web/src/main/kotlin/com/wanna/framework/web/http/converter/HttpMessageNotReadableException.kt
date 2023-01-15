package com.wanna.framework.web.http.converter

import com.wanna.framework.web.http.HttpInputMessage

/**
 * HttpMessage没有可读异常, 当使用HttpMessageConverter去进行读取RequestBody时, 读取不到RequestBody当中的数据时, 但是参数又是必须要的, 那么抛出该异常
 *
 * @see HttpMessageConverter
 * @see com.wanna.framework.web.http.converter.json.MappingJackson2HttpMessageConverter
 */
open class HttpMessageNotReadableException(msg: String, val inputMessage: HttpInputMessage) : RuntimeException(msg)