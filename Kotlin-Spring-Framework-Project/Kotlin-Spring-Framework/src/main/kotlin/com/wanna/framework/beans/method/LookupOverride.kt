package com.wanna.framework.beans.method

import java.lang.reflect.Method

/**
 * 这是一种运行时的方法重写
 */
class LookupOverride(val method: Method, val beanName: String) : MethodOverride(method.name) {
    override fun matches(method: Method): Boolean {
        return this.method == method
    }
}