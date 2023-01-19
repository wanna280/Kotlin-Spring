package com.wanna.framework.transaction.annotation

import com.wanna.framework.core.annotation.AliasFor
import com.wanna.framework.transaction.TransactionDefinition
import kotlin.reflect.KClass

/**
 * 标识这是一个事务方法, 当然, 也可以标注在类上表示该类的所有方法都拥有事务功能
 *
 * 要使用事务功能, 必须得使用`EnableTransactionManagement`, 去开启Spring事务的支持, 如下代码所示
 *
 * ```kotlin
 * @Component
 * @EnableTransactionManagement
 * class App
 * ```
 *
 * @see EnableTransactionManagement
 *
 * @param value 指定要使用的TransactionManager的beanName, 同transactionManager
 * @param transactionManager 指定要使用的TransactionManager的beanName, 同value
 * @param timeout 事务的超时时间
 * @param readOnly 事务是否只读? 设置为只读会提高性能, 但是如果出现写操作会抛出异常
 * @param rollbackFor 遇到哪个异常时需要回滚(指定Class)
 * @param rollbackForClassName 遇到哪个异常时需要回滚? (指定className)
 * @param noRollbackFor 遇到哪个异常时不需要回滚? (指定Class)
 * @param noRollbackForClassName 遇到哪个异常时不需要回滚? (指定className)
 * @param isolation 事务的隔离级别(默认使用数据库的隔离级别)
 * @param propagation 事务的传播属性(默认为每个事务方法创建一个新的事务)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Transactional(
    @get:AliasFor("transactionManager")
    val value: String = "",
    @get:AliasFor("value")
    val transactionManager: String = "",
    val timeout: Int = TransactionDefinition.TIMEOUT_DEFAULT,
    val readOnly: Boolean = false,
    val rollbackFor: Array<KClass<out Throwable>> = [],
    val rollbackForClassName: Array<String> = [],
    val noRollbackFor: Array<KClass<out Throwable>> = [],
    val noRollbackForClassName: Array<String> = [],
    val isolation: Isolation = Isolation.DEFAULT,
    val propagation:Propagation = Propagation.REQUIRED
)
