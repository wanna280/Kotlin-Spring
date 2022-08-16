package com.wanna.framework.validation

interface Errors {
    fun getObjectName(): String

    fun reject(errorCode: String)

    fun reject(errorCode: String, defaultMessage: String)

    fun hasErrors() : Boolean

    fun getAllErrors(): List<ObjectError>

    fun getFieldError(name: String): FieldError?
}