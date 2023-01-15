package com.wanna.framework.validation

/**
 * 概述一个Object Error, 去拒绝这个Object的全局原因
 */
open class ObjectError {
    private var source: Any? = null

    open fun wrap(source: Any) {
        this.source = source
    }
}