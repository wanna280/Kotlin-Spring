package com.wanna.framework.beans.annotations

/**
 * 标识这是一个带有优先级的组件
 */
interface Ordered {

    // 最高优先级
    val ORDER_HIGHEST: Int
        get() = Int.MIN_VALUE

    // 最低优先级
    val ORDER_LOWEST: Int
        get() = Int.MAX_VALUE

    /**
     * 获取Order
     */
    fun getOrder(): Int
}