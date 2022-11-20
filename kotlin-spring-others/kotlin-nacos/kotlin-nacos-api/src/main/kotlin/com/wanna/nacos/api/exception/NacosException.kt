package com.wanna.nacos.api.exception

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
class NacosException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}