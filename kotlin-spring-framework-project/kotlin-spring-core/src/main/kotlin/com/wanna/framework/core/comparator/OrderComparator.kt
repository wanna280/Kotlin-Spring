package com.wanna.framework.core.comparator

import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered

/**
 * 这是一个Order的比较器, 用来对Bean按照顺序去进行排序
 * (1)对于实现PriorityOrdered的Bean, 拥有最高优先级
 * (2)对于实现Ordered的Bean, 拥有一般优先级
 * (3)对于没有实现Ordered/PriorityOrdered的Bean, 拥有最低的优先级
 */
open class OrderComparator : Comparator<Any?> {

    companion object {
        @JvmField
        val INSTANCE = OrderComparator()

        /**
         * 按照Order去完成排序
         */
        @JvmStatic
        fun sort(list: MutableList<*>) {
            list.sortWith(INSTANCE)
        }
    }

    override fun compare(o1: Any?, o2: Any?): Int {
        val p1 = o1 is PriorityOrdered
        val p2 = o2 is PriorityOrdered

        // 如果其中一个实现PriorityOrdered, 另外一个没实现PriorityOrdered的情况
        // 肯定是实现PriorityOrdered的情况下优先级比较高, 可以直接return
        if (p1 && !p2) {
            return -1
        } else if (!p1 && p2) {
            return 1
        }
        // 如果两者都实现了PriorityOrdered, 或者两者都只是实现了普通的Ordered, 那么就需要进行比较
        // 针对于@Order注解, 也可以当做Ordered去进行使用, 但是这个是交给子类去进行实现的...
        // 子类只需要自定义寻找Order的逻辑, 即可让子类拥有Order的能力...
        val order1 = getOrder(o1)
        val order2 = getOrder(o2)
        return order1.compareTo(order2)
    }

    /**
     * 从给定的对象当中去寻找Order
     * (1)如果给定的对象为null, 那么return 最低优先级
     * (2)如果给定的对象不为null, 需要从对象当中去进行获取Order, 如果也没找到Order, return 最低优先级
     */
    private fun getOrder(obj: Any?): Int {
        if (obj != null) {
            val order = findOrder(obj)  // 从对象当中寻找Order
            if (order != null) {
                return order
            }
        }
        return Ordered.ORDER_LOWEST
    }

    /**
     * 获取优先级
     */
    open fun getPriority(obj: Any?) : Int {
        return getOrder(obj)
    }

    /**
     * 通过这个模板方法去自定义寻找Order的方式, 默认实现为从Ordered的getOrder方法的返回值去进行获取...
     * 子类当中可以扩展这个方法, 完成自定义的Order的获取方式, 比如可以新增@Order注解的Order的方式去进行获取
     */
    protected open fun findOrder(obj: Any?) = if (obj is Ordered) obj.getOrder() else null
}