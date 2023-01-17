package com.wanna.boot.actuate.endpoint.invoke

/**
 * 封装Operation方法的其中一个参数的参数信息
 */
interface OperationParameter {

    /**
     * 获取Operation参数的参数名
     *
     * @return 参数名
     */
    fun getName(): String

    /**
     * 获取Operation参数的参数类型
     *
     * @return 参数类型
     */
    fun getType(): Class<*>

    /**
     * 该参数是否是强制性的? (该参数是否允许为空? )
     *
     * @return 如如果该参数不允许为空, return true; 否则return false
     */
    fun isMandatory(): Boolean
}