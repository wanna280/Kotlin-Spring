package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.interceptor.TransactionAttribute
import java.lang.reflect.AnnotatedElement

/**
 * 事务注解的解析器, 负责解析来自各个地方的@Transactional注解
 *
 * @see SpringTransactionAnnotationParser
 */
interface TransactionAnnotationParser {

    /**
     * 判断目标类是否是一个合格的事务类
     *
     * @param targetClass targetClass
     * @return 如果它是一个合格的事务类, return true; 不然return false
     */
    fun isCandidateClass(targetClass: Class<*>): Boolean

    /**
     * 从目标方法或者目标类上去解析事务注解, 并封装成为事务属性信息
     *
     * @param element 目标方法/类
     * @return 事务属性信息(如果不存在事务注解的话, return null)
     */
    fun parseTransactionAnnotation(element:AnnotatedElement) : TransactionAttribute?
}