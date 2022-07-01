package com.wanna.framework.transaction

open class TransactionException(msg: String, ex: Throwable) : RuntimeException(msg, ex) {
}