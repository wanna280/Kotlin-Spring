package com.wanna.framework.beans.method

import java.lang.reflect.Method

/**
 * 这种一个运行时的方法重写
 */
class ReplaceOverride(methodName: String, val replacerBeanName: String) : MethodOverride(methodName) {
    override fun matches(method: Method): Boolean {
        return methodName == method.name
    }
}