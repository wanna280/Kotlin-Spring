package com.wanna.framework.transaction.support

import com.wanna.framework.transaction.TransactionStatus

abstract class AbstractTransactionStatus : TransactionStatus {

    // 当前事务是否已经完成了？
    private var completed = false

    override fun isCompleted() = completed

    open fun setCompleted() {
        this.completed = true
    }
}