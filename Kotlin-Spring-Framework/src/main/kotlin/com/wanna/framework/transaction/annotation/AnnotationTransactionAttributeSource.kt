package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.interceptor.AbstractFallbackTransactionAttributeSource
import com.wanna.framework.transaction.interceptor.TransactionAttribute
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 基于注解的Spring注解属性源，提供事务属性的来源，从@Transactional的方法/类上去进行寻找
 *
 * @param publicMethodsOnly 是否只允许public方法拥有事务？默认为true
 */
open class AnnotationTransactionAttributeSource(private val publicMethodsOnly: Boolean = true) :
    AbstractFallbackTransactionAttributeSource() {

    // @Transactional的注解解析器，支持使用各种策略去解析不同的@Transactional，比如Spring的@Transactional以及javax当中的@Transactional
    private val annotationParsers: MutableSet<TransactionAnnotationParser> = HashSet()

    init {
        // 添加Spring的@Transactional的注解解析器
        annotationParsers += SpringTransactionAnnotationParser()
    }

    override fun isCandidateClass(targetClass: Class<*>): Boolean {
        annotationParsers.forEach {
            if (it.isCandidateClass(targetClass)) {
                return true
            }
        }
        return false
    }

    override fun findTransactionAttribute(targetClass: Class<*>) = determineTransactionAttribute(targetClass)

    override fun findTransactionAttribute(method: Method) = determineTransactionAttribute(method)

    /**
     * 使用策略模式，从各个目标类/方法上寻找@Transactional注解，并封装成为TransactionAttribute
     *
     * @param element 类/方法
     * @return 如果找到了注解，那么return TransactionAttribute；如果没有找到，return false
     */
    protected open fun determineTransactionAttribute(element: AnnotatedElement): TransactionAttribute? {
        annotationParsers.forEach {
            val transactionAttribute = it.parseTransactionAnnotation(element)
            if (transactionAttribute != null) {
                return transactionAttribute
            }
        }
        return null
    }

    override fun allowPublicMethodsOnly() = this.publicMethodsOnly
}