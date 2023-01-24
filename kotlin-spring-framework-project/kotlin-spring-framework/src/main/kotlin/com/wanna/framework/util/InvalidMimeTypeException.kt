package com.wanna.framework.util

/**
 *  不合法的[MimeType]异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/24
 */
open class InvalidMimeTypeException(val mimeType: String, message: String) :
    IllegalArgumentException("Invalid mime type \"$mimeType\": $message")