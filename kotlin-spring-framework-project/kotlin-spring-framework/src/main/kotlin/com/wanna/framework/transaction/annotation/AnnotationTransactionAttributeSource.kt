package com.wanna.framework.transaction.annotation

import com.wanna.framework.transaction.interceptor.AbstractFallbackTransactionAttributeSource
import com.wanna.framework.transaction.interceptor.TransactionAttribute
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 基于注解的Spring注解属性源, 它提供事务属性的来源, 从@Transactional的方法/类上去进行寻找和匹配;
 * 委托了TransactionAnnotationParser去提供匹配@Transactional注解的规则
 *
 * @param publicMethodsOnly 是否只允许public方法拥有事务？默认为true
 *
 * @see TransactionAttribute
 * @see TransactionAnnotationParser
 */
open class AnnotationTransactionAttributeSource(private val publicMethodsOnly: Boolean = true) :
    AbstractFallbackTransactionAttributeSource() {

    // @Transactional的注解解析器, 支持使用各种策略去解析不同的@Transactional, 比如Spring的@Transactional以及javax当中的@Transactional
    private val annotationParsers: MutableSet<TransactionAnnotationParser> = HashSet()

    init {
        // 添加Spring的@Transactional的注解解析器
        annotationParsers += SpringTransactionAnnotationParser()
    }

    /**
     * 使用策略模式, 遍历所有的TransactionAnnotationParser, 去判断候选类是否是有资格作为要去进行使用的事务类;
     * 也就是委托TransactionAnnotationParser去完成匹配
     *
     * @param targetClass 要去进行匹配的目标类
     * @return 如果是候选的可以作为事务的类型, return true; 否则return false
     */
    override fun isCandidateClass(targetClass: Class<*>): Boolean {
        annotationParsers.forEach {
            if (it.isCandidateClass(targetClass)) {
                return true
            }
        }
        return false
    }

    /**
     * 尝试从类上寻找@Transactional注解, 封装成为TransactionAttribute
     *
     * @param targetClass 目标类
     * @return 从@Transactional解析到的TransactionAttribute
     */
    override fun findTransactionAttribute(targetClass: Class<*>) = determineTransactionAttribute(targetClass)

    /**
     * 尝试从方法上寻找@Transactional注解, 封装成为TransactionAttribute
     *
     * @param method 目标方法
     * @return 从@Transactional解析到的TransactionAttribute
     */
    override fun findTransactionAttribute(method: Method) = determineTransactionAttribute(method)

    /**
     * 使用策略模式, 从各个目标类/方法上寻找@Transactional注解, 如果找到了合适的@Transactional, 需要封装成为TransactionAttribute
     *
     * @param element 要去进行匹配的目标类/方法
     * @return 如果找到了@Transactional注解, 那么return TransactionAttribute; 如果没有找到, return false
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

    /**
     * 是否只允许public方法生成事务
     *
     * @return 如果确实只允许public方法生成事务, 那么return true; 否则, return false
     */
    override fun allowPublicMethodsOnly() = this.publicMethodsOnly
}