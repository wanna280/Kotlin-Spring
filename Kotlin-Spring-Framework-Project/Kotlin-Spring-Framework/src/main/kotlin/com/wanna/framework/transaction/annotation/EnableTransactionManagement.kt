package com.wanna.framework.transaction.annotation

import com.wanna.framework.context.annotation.Import
import com.wanna.framework.core.Ordered

/**
 * 开始Spring的事务的支持，开启了Spring事务的支持之后，可以使用@Transactional注解对某个方法提供事务的支持
 *
 * @param order 设置优先级，其实是设置的事务的Advisor的order
 *
 * @see Transactional
 */
@Import([TransactionManagementConfigurationSelector::class])
@Target(AnnotationTarget.CLASS)
annotation class EnableTransactionManagement(val order: Int = Ordered.ORDER_LOWEST)
