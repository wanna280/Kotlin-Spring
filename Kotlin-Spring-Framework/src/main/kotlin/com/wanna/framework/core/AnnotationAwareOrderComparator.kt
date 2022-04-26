package com.wanna.framework.core

import com.wanna.framework.beans.annotations.Order
import org.springframework.core.annotation.AnnotatedElementUtils


/**
 * 这是一个支持处理注解版的Order的比较器，不仅支持了Ordered/PriorityOrdered，也对@Order注解提供了支持
 */
open class AnnotationAwareOrderComparator : OrderComparator() {

    companion object {
        @JvmField
        val INSTANCE = AnnotationAwareOrderComparator()

        /**
         * 按照注解版的Order去完成排序
         */
        @JvmStatic
        fun sort(list: MutableList<*>) {
            list.sortWith(INSTANCE)
        }
    }

    /**
     * 自定义寻找Order的方式，先检查Ordered，再去检查@Order注解
     */
    override fun findOrder(obj: Any?): Int? {
        if (obj == null) {
            return null
        }
        // 利用父类的提供的方式，去检查Ordered
        val order = super.findOrder(obj)
        if (order != null) {
            return order
        }
        // 新增从注解的支持，去检查@Order注解
        return findOrderFromAnnotation(obj)
    }

    /**
     * 从注解当中寻找Order，如果没有找到，那么return null
     */
    private fun findOrderFromAnnotation(obj: Any) =
        AnnotatedElementUtils.getMergedAnnotation(obj::class.java, Order::class.java)?.value


}