package com.wanna.boot.actuate.endpoint

/**
 * Endpoint的操作的最顶层接口，描述了一个Endpoint当中标注了Operation相关的注解的方法
 *
 * @see OperationType
 * @see com.wanna.boot.actuate.endpoint.annotation.ReadOperation
 * @see com.wanna.boot.actuate.endpoint.annotation.WriteOperation
 * @see com.wanna.boot.actuate.endpoint.annotation.DeleteOperation
 */
interface Operation {

    /**
     * 获取该方法支持去进行操作的类型(READ/WRITE/DELETE)
     *
     * @return OperationType
     */
    fun getType(): OperationType

    /**
     * 执行目标Operation方法
     *
     * @param context InvocationContext(执行目标方法需要用到的方法列表)
     * @return 执行目标操作方法的返回值
     */
    fun invoke(context: InvocationContext): Any?
}