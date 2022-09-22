package com.wanna.framework.validation

open class FieldError : ObjectError() {
    var isBindingFailure = false
}