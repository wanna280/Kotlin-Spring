package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.lang.Nullable


/**
 * 这是一个与ConditionOutcome相关联的Message
 *
 * @param message 匹配成功/失败的消息
 */
open class ConditionMessage @JvmOverloads constructor(val message: String? = null) {

    override fun toString(): String = message ?: ""

    companion object {
        @JvmStatic
        fun of(@Nullable message: String?): ConditionMessage {
            return ConditionMessage(message)
        }

        @JvmStatic
        fun empty(): ConditionMessage {
            return ConditionMessage()
        }
    }

}