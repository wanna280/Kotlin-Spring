package com.wanna.framework.transaction

interface TransactionExecution {

    fun isNewTransaction() : Boolean

    fun isCompleted() :Boolean
}