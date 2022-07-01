package com.wanna.boot.actuate.endpoint

import com.wanna.framework.core.environment.Environment

/**
 * EndpointId
 */
open class EndpointId(val value: String) {
    companion object {
        @JvmStatic
        fun of(environment: Environment, value: String): EndpointId {
            return EndpointId(value)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EndpointId
        if (value != other.value) return false
        return true
    }

    override fun hashCode() = value.hashCode()
    override fun toString() = this.value  // toString
}