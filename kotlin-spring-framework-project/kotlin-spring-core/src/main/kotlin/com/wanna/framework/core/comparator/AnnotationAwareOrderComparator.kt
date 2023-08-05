package com.wanna.framework.core.comparator

import com.wanna.framework.core.Order
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.util.ClassUtils


/**
 * 这是一个支持处理注解版的Order的比较器, 不仅支持了Ordered/PriorityOrdered, 也对@Order注解提供了支持
 */
open class AnnotationAwareOrderComparator : OrderComparator() {
    companion object {
        @JvmField
        val INSTANCE = AnnotationAwareOrderComparator()

        private const val PRIORITY_ANNOTATION = "javax.annotation.Priority"

        /**
         * 按照注解版的Order去完成排序
         */
        @JvmStatic
        fun sort(list: MutableList<*>) {
            list.sortWith(INSTANCE)
        }
    }

    /**
     * 自定义寻找Order的方式, 先检查Ordered, 再去检查@Order注解
     *
     * @param obj 要寻找Order的目标对象
     */
    override fun findOrder(obj: Any?): Int? {
        if (obj == null) {
            return null
        }
        // 利用父类的提供的方式, 去检查Ordered的情况
        val order = super.findOrder(obj)
        if (order != null) {
            return order
        }
        // 新增从注解的支持, 去检查@Order/@Priority注解
        return findOrderFromAnnotation(obj)
    }

    /**
     * 从注解当中寻找Order, 如果没有找到, 那么return null
     */
    private fun findOrderFromAnnotation(obj: Any): Int? {
        // first to check @Order
        val orderAnnotation = AnnotatedElementUtils.getMergedAnnotationAttributes(obj::class.java, Order::class.java)
        if (orderAnnotation != null) {
            return orderAnnotation.getInt(MergedAnnotation.VALUE)
        }
        // second to check @Priority
        val priorityAnnotation =
            AnnotatedElementUtils.getMergedAnnotationAttributes(obj.javaClass, ClassUtils.forName(PRIORITY_ANNOTATION))
        if (priorityAnnotation != null) {
            return priorityAnnotation.getInt("order")
        }
        return null
    }


}