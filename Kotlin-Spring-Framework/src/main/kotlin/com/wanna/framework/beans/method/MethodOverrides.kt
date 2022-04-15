package com.wanna.framework.beans.method

/**
 * 这里维护了一个MethodOverride的列表，也就是要进行运行时方法重写的列表
 */
open class MethodOverrides() {

    constructor(methodOverrides: MethodOverrides) : this() {
        methodOverrides.methodOverrides.forEach(this::addMethodOverride)
    }

    private val methodOverrides: MutableSet<MethodOverride> = HashSet()

    open fun addMethodOverride(methodOverride: MethodOverride) {
        methodOverrides += methodOverride
    }

    open fun getMethodOverrides(): MutableSet<MethodOverride> {
        return this.methodOverrides
    }

    open fun removeMethodOverride(methodName: String) {
        methodOverrides.removeIf {
            it.methodName == methodName
        }
    }

}