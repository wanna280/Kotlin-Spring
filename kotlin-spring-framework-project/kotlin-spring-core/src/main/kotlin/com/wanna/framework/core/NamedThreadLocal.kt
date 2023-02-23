package com.wanna.framework.core

/**
 * 支持有name的ThreadLocal
 *
 * @param name thread local name
 */
open class NamedThreadLocal<T>(val name: String) : ThreadLocal<T>() {
    override fun toString() = this.name
}