package com.wanna.framework.core

/**
 * 标识这是一个带有优先级的SpringBean, 与[Order]注解作用相同
 *
 * @see Order
 */
fun interface Ordered {

    companion object {
        /**
         * 最高优先级
         */
        const val ORDER_HIGHEST = Int.MIN_VALUE

        /**
         * 最低优先级
         */
        const val ORDER_LOWEST = Int.MAX_VALUE
    }

    /**
     * 获取当前SpringBean的优先级
     *
     * Note: 数字越小优先级越高, 数字越大优先级越低
     *
     * @return order
     */
    fun getOrder(): Int
}