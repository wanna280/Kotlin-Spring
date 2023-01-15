package com.wanna.framework.transaction.interceptor

import java.lang.reflect.Method

/**
 * 事务的属性源, 负责提供事务的属性的来源; 比如扫描@Transactional注解, 去封装成为TransactionAttribute
 *
 * @see com.wanna.framework.transaction.annotation.AnnotationTransactionAttributeSource
 */
interface TransactionAttributeSource {
    fun isCandidateClass(targetClass: Class<*>): Boolean = true
    fun getTransactionAttribute(method: Method, targetClass: Class<*>?): TransactionAttribute?
}