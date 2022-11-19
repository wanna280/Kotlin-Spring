package com.wanna.framework.core

/**
 * 标识这是一个带有优先级的组件
 */
fun interface Ordered {

    companion object {
        const val ORDER_HIGHEST = Int.MIN_VALUE  // 最高优先级
        const val ORDER_LOWEST = Int.MAX_VALUE  // 最低优先级
    }

    /**
     * 获取Order
     */
    fun getOrder(): Int
}