package com.wanna.framework.core

import com.wanna.framework.beans.annotations.Order
import org.springframework.core.annotation.AnnotatedElementUtils


/**
 * 这是一个支持处理注解版的Order的比较器
 */
open class AnnotationOrderComparator : OrderComparator() {

    companion object {
        @JvmStatic
        val INSTANCE = AnnotationOrderComparator()
    }

    override fun findOrder(obj: Any?): Int? {
        if (obj == null) {
            return null
        }
        // 检查Ordered
        val order = super.findOrder(obj)
        if (order != null) {
            return order
        }
        // 检查@Order注解
        return findOrderFromAnnotation(obj)
    }

    /**
     * 从注解当中寻找Order，如果没有找到，那么return null
     */
    private fun findOrderFromAnnotation(obj: Any): Int? {
        return AnnotatedElementUtils.getMergedAnnotation(obj::class.java, Order::class.java)?.value ?: return null
    }
}