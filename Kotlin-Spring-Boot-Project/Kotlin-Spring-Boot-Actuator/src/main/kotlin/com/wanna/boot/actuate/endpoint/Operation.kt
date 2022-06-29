package com.wanna.boot.actuate.endpoint

/**
 * Endpoint的操作的顶层接口
 */
interface Operation {

    /**
     * 获取该操作的类型(READ/WRITE/DELETE)
     *
     * @return 操作类型
     */
    fun getType(): OperationType

    /**
     * 执行目标Operation
     *
     * @param context InvocationContext
     * @return 执行目标操作方法的返回值
     */
    fun invoke(context: InvocationContext): Any?
}