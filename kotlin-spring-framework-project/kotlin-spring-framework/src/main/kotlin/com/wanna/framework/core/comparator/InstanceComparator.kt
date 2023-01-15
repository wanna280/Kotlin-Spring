package com.wanna.framework.core.comparator

/**
 * 这是一个实例的比较器, 给定一个类型数组, 给定的实例的类型clazz位于数组当中的index作为索引, 去进行比较
 */
class InstanceComparator<T>(private val instanceOrder: Array<Class<*>>) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        return getOrder(o1).compareTo(getOrder(o2))
    }

    private fun getOrder(o: T): Int {
        instanceOrder.indices.forEach {
            if (instanceOrder[it].isInstance(o)) {
                return it
            }
        }
        return instanceOrder.size  // return order MinValue
    }
}