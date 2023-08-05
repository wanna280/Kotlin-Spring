package com.wanna.framework.beans.method

import java.lang.reflect.Method

/**
 * 这里维护了一个MethodOverride的列表, 也就是要进行运行时方法重写的方法列表
 *
 * @see MethodOverride
 * @see ReplaceOverride
 * @see LookupOverride
 */
open class MethodOverrides() {

    private val methodOverrides: MutableSet<MethodOverride> = HashSet()

    constructor(methodOverrides: MethodOverrides) : this() {
        methodOverrides.methodOverrides.forEach(this::addMethodOverride)
    }

    open fun addMethodOverride(methodOverride: MethodOverride) {
        methodOverrides += methodOverride
    }

    open fun getMethodOverrides() = this.methodOverrides

    /**
     * 根据方法名去移除MethodOverride列表当中的一个MethodOverride
     *
     * @param methodName 方法名
     */
    open fun removeMethodOverride(methodName: String) {
        methodOverrides.removeIf { it.methodName == methodName }
    }

    /**
     * 根据方法, 去匹配MethodOverride; 
     * 如果匹配到, return匹配到的MethodOverride; 不然return null
     */
    open fun getMethodOverride(method: Method): MethodOverride? {
        for (methodOverride in methodOverrides) {
            if (methodOverride.matches(method)) {
                return methodOverride
            }
        }
        return null
    }

}