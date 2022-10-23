package com.wanna.framework.beans.method

import java.lang.reflect.Method

/**
 * 这是一种运行时的方法重写
 */
class LookupOverride(methodName: String, val beanName: String) :
    MethodOverride(methodName) {

    private var method: Method? = null

    constructor(method: Method, beanName: String) : this(method.name, beanName) {
        this.method = method
    }

    override fun matches(method: Method): Boolean {
        return this.method == method
    }
}