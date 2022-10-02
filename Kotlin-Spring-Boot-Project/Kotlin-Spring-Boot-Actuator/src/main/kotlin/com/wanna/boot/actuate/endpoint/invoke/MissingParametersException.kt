package com.wanna.boot.actuate.endpoint.invoke

import com.wanna.boot.actuate.endpoint.InvalidEndpointRequestException
import com.wanna.framework.util.StringUtils

/**
 * Endpoint的Operation方法缺少参数异常
 *
 * @param missedParameters 缺少的参数列表
 * @param message message
 * @param reason reason
 * @param cause cause
 */
open class MissingParametersException(
    val missedParameters: List<OperationParameter>,
    message: String,
    reason: String,
    cause: Throwable? = null
) : InvalidEndpointRequestException(message, reason, cause) {
    constructor(missedParameters: List<OperationParameter>) : this(
        missedParameters,
        "执行目标Operation方法失败，原因是下面这些参数[${
            StringUtils.collectionToCommaDelimitedString(missedParameters.map { it.toString() }.toList())
        }]都确实必须存在，但是实际上没有传递",
        "缺少参数列表[${missedParameters.map { it.getName() }.toList().joinToString(",")}]",
        null
    )
}