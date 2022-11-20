package com.wanna.nacos.api.model

import com.wanna.nacos.api.http.param.Header

/**
 * Http的RestResult
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
open class HttpRestResult<T : Any>(
    var header: Header,
    code: Int,
    data: T?,
    message: String?
) : RestResult<T>(code, data, message)