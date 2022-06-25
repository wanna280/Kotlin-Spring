package com.wanna.framework.transaction.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.transaction.interceptor.*
import java.lang.reflect.AnnotatedElement

/**
 * Spring事务的注解解析器，负责解析Spring家的@Transactional注解，并封装成为TransactionAttribute
 *
 * @see TransactionAnnotationParser
 */
open class SpringTransactionAnnotationParser : TransactionAnnotationParser {

    override fun isCandidateClass(targetClass: Class<*>) = true

    /**
     * 解析类/方法上的@Transactional注解
     *
     * @param element 要解析注解的目标元素(类/方法)
     */
    override fun parseTransactionAnnotation(element: AnnotatedElement): TransactionAttribute? {
        val transactional = AnnotatedElementUtils.getMergedAnnotation(element, Transactional::class.java) ?: return null
        val attributes = AnnotationAttributesUtils.asAnnotationAttributes(transactional) ?: return null
        return parseTransactionAnnotation(attributes)
    }

    /**
     * 解析@Transactional注解，将该注解当中的相关属性封装到TransactionAttribute当中
     *
     * @param transactional @Transactional注解
     * @return 事务相关的属性信息
     */
    protected open fun parseTransactionAnnotation(transactional: Transactional): TransactionAttribute {
        return parseTransactionAnnotation(AnnotationAttributesUtils.asAnnotationAttributes(transactional)!!)
    }

    /**
     * 解析@Transactional注解，将该注解当中的相关属性封装到TransactionAttribute当中
     *
     * @param attributes @Transactional注解当中的属性信息
     * @return 事务相关的属性信息
     */
    protected open fun parseTransactionAnnotation(attributes: AnnotationAttributes): TransactionAttribute {
        val transactionAttribute = RuleBasedTransactionAttribute()

        val transactionManager = attributes.getString("transactionManager")!!
        val timeout = attributes.getInt("timeout")
        val readOnly = attributes.getBoolean("readOnly")
        val isolation = attributes.getForType("isolation", Isolation::class.java)!!
        val propagation = attributes.getForType("propagation", Propagation::class.java)!!
        val rollbackForClassName = attributes.getStringArray("rollbackForClassName")!!
        val rollbackForClass = attributes.getClassArray("rollbackFor")!!
        val noRollbackForClassName = attributes.getStringArray("noRollbackForClassName")!!
        val noRollbackForClass = attributes.getClassArray("noRollbackFor")!!

        // 收集所有的RollBack的规则
        val rollbackRuleAttributes = ArrayList<RollbackRuleAttribute>()

        // add rollback for
        rollbackForClassName.forEach { rollbackRuleAttributes.add(RollbackRuleAttribute(it)) }
        rollbackForClass.forEach { rollbackRuleAttributes.add(RollbackRuleAttribute(it)) }

        // add no rollback for
        noRollbackForClassName.forEach { rollbackRuleAttributes += NoRollbackRuleAttribute(it) }
        noRollbackForClass.forEach { rollbackRuleAttributes += NoRollbackRuleAttribute(it) }

        transactionAttribute.setQualifier(transactionManager)
        transactionAttribute.setTimeout(timeout)
        transactionAttribute.setReadOnly(readOnly)
        transactionAttribute.setIsolationLevel(isolation.level)
        transactionAttribute.setPropagationBehavior(propagation.value)
        transactionAttribute.setRollbackRules(rollbackRuleAttributes)
        return transactionAttribute
    }
}