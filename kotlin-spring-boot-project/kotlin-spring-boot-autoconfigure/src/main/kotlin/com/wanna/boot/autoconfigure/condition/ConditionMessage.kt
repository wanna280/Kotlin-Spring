package com.wanna.boot.autoconfigure.condition

/**
 * 这是一个与ConditionOutcome相关联的Message
 */
open class ConditionMessage(val message: String?) {
    constructor() : this(null)

    companion object {
        @JvmStatic
        fun of(message: String?): ConditionMessage {
            return ConditionMessage(message)
        }

        @JvmStatic
        fun empty(): ConditionMessage {
            return ConditionMessage()
        }
    }

}