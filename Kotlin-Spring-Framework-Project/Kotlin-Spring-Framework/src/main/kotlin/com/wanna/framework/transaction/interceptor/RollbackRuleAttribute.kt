package com.wanna.framework.transaction.interceptor

/**
 * 这是一个Spring事务的一个回滚的规则
 *
 * @param exceptionName 哪个异常要回滚？
 */
open class RollbackRuleAttribute(private val exceptionName: String) {
    constructor(exClass: Class<*>) : this(exClass.name)

    /**
     * 遍历指定的异常的所有父类，判断几级父类可以匹配当前的exceptionName？
     * 如果到达了Throwable都不匹配了，那么return -1
     *
     * @param exceptionClass 要去进行匹配的异常类
     * @return 异常所在的深度(不匹配return -1)
     */
    open fun getDepth(exceptionClass: Class<*>): Int {
        return getDepth(exceptionClass, 0)
    }

    /**
     * 遍历指定的异常的所有父类，判断几级父类可以匹配当前的exceptionName？
     * 如果到达了Throwable都不匹配了，那么return -1
     *
     * @param exceptionClass 异常类
     * @param depth 深度
     * @return 最终深度(如果异常都不匹配，那么return -1)
     */
    private fun getDepth(exceptionClass: Class<*>, depth: Int): Int {
        if (exceptionClass.name.contains(exceptionName)) {
            return depth
        }
        if (exceptionClass == Throwable::class.java) {
            return -1
        }
        return getDepth(exceptionClass.superclass, depth + 1)
    }
}