package com.wanna.framework.transaction.support

interface ResourceHolder {
    fun reset()

    fun unbound()

    fun isVoid() :Boolean
}