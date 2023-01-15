package com.wanna.framework.transaction.support

/**
 * 包装了一个资源(比如JDBC连接)的Holder
 */
interface ResourceHolder {

    /**
     * 重设资源的状态
     */
    fun reset()

    /**
     * 解除资源的绑定
     */
    fun unbound()

    /**
     * 判断Holder当中是否已经没有资源了？
     *
     * @return 如果Holder当中已经为空了, 那么return true; 否则return false
     */
    fun isVoid(): Boolean
}