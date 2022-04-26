package com.wanna.framework.beans.method

import java.lang.reflect.Method

/**
 * 这是一种运行时的方法重写
 */
class LookupOverride(method: Method, var beanName: String) : MethodOverride(method.name)