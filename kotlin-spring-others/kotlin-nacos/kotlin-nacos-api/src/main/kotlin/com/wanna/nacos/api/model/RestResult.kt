package com.wanna.nacos.api.model

import java.io.Serializable

/**
 * RestResult
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
open class RestResult<T : Any>(
    val code: Int,
    val data: T?,
    val message: String? = null
) : Serializable {

    /**
     * 判断是否是成功?
     *
     * @return 如果code=0/200, 那么return true; 否则return false
     */
    open fun ok(): Boolean = code == 0 || code == 200
}