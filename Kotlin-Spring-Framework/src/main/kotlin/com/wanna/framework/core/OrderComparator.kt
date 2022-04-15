package com.wanna.framework.core

import com.wanna.framework.beans.annotations.Ordered
import com.wanna.framework.beans.annotations.PriorityOrdered

/**
 * 这是一个Order的比较器，用来对Bean按照顺序去进行排序
 */
open class OrderComparator : Comparator<Any?> {

    companion object {
        @JvmStatic
        val INSTANCE = OrderComparator()
    }

    override fun compare(o1: Any?, o2: Any?): Int {
        val p1 = o1 is PriorityOrdered
        val p2 = o2 is PriorityOrdered
        if (p1 && !p2) {
            return -1
        } else if (!p1 && p2) {
            return 1
        }
        val order1 = getOrder(o1)
        val order2 = getOrder(o2)
        return order1.compareTo(order2)
    }

    private fun getOrder(obj: Any?): Int {
        if (obj != null) {
            val order = findOrder(obj)
            if (order != null) {
                return order
            }
        }
        return Ordered.ORDER_LOWEST
    }

    protected open fun findOrder(obj: Any?): Int? {
        return if (obj is Ordered) obj.getOrder() else null
    }
}