package com.wanna.framework.transaction.interceptor

/**
 * 在默认的基础上新增基于规则的事务属性，新增自定义使用某个规则去进行判断该事务是否需要去进行回滚；
 * 要去进行匹配的规则，可以使用RollbackRuleAttribute/NoRollbackRuleAttribute两类
 *
 * @see RollbackRuleAttribute
 * @see NoRollbackRuleAttribute
 * @see DefaultTransactionAttribute
 */
open class RuleBasedTransactionAttribute : DefaultTransactionAttribute() {

    // 需要去进行回滚的规则
    private var rollbackRules: List<RollbackRuleAttribute>? = null

    open fun setRollbackRules(rollbackRules: List<RollbackRuleAttribute>) {
        this.rollbackRules = rollbackRules
    }

    open fun getRollbackRules(): List<RollbackRuleAttribute> {
        var rollbackRules = this.rollbackRules
        if (rollbackRules == null) {
            rollbackRules = ArrayList()
            this.rollbackRules = rollbackRules
        }
        return rollbackRules
    }

    override fun rollbackOn(ex: Throwable): Boolean {
        var winner: RollbackRuleAttribute? = null
        // 记录最小深度的异常(初始化为Int.MAX)
        var deepest = Int.MAX_VALUE
        // 遍历所有的Rollback规则，判断哪个规则是最匹配的
        // 看ex的几级父类，可以匹配到指定的规则？找到深度最小的一个规则作为目标规则
        this.rollbackRules?.forEach {
            val depth = it.getDepth(ex::class.java)
            if (depth in 0 until deepest) {
                deepest = depth
                winner = it
            }
        }
        // 如果没有找到匹配的，那么使用super.rollbackOn
        // 如果找到匹配的，如果它是NoRollbackRuleAttribute的话，return false；如果它不是NoRollbackRuleAttribute，则return true
        return if (winner == null) super.rollbackOn(ex) else winner !is NoRollbackRuleAttribute
    }
}