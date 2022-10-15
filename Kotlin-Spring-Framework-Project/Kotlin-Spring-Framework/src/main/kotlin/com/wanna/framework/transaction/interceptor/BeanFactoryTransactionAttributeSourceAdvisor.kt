package com.wanna.framework.transaction.interceptor

import com.wanna.framework.aop.support.AbstractBeanFactoryPointcutAdvisor

/**
 * 基于BeanFactory的事务属性源的Advisor，可以设置Advice或者adviceBeanName
 *
 * @see AbstractBeanFactoryPointcutAdvisor
 */
open class BeanFactoryTransactionAttributeSourceAdvisor : AbstractBeanFactoryPointcutAdvisor() {

    // 事务属性源
    private var transactionAttributeSource: TransactionAttributeSource? = null

    // 获取Pointcut，重写getTransactionAttributeSource方法，让Pointcut能使用TransactionAttributeSource去完成匹配
    override fun getPointcut() = object : TransactionAttributeSourcePointcut() {
        override fun getTransactionAttributeSource(): TransactionAttributeSource? =
            this@BeanFactoryTransactionAttributeSourceAdvisor.getTransactionAttributeSource()
    }

    open fun setTransactionAttributeSource(transactionAttributeSource: TransactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource
    }

    open fun getTransactionAttributeSource(): TransactionAttributeSource? {
        return this.transactionAttributeSource
    }
}