package com.wanna.boot.autoconfigure.condition

/**
 * 这是一个SpringBootCondition的匹配结果的一层抽象, Outcome(结果)-意思和Result基本一致,差不多可以通用
 *
 * @see SpringBootCondition
 */
open class ConditionOutcome(val match: Boolean, val message: ConditionMessage) {
    constructor(match: Boolean, message: String?) : this(match, ConditionMessage.of(message))

    /**
     * 提供ConditionOutCome的快速构建的static工厂方法
     */
    companion object {
        @JvmStatic
        fun match(): ConditionOutcome {
            return match(ConditionMessage.empty())
        }

        @JvmStatic
        fun match(message: String?): ConditionOutcome {
            return ConditionOutcome(true, message)
        }

        @JvmStatic
        fun match(message: ConditionMessage): ConditionOutcome {
            return ConditionOutcome(true, message)
        }

        @JvmStatic
        fun noMatch(): ConditionOutcome {
            return noMatch(ConditionMessage.empty())
        }

        @JvmStatic
        fun noMatch(message: String?): ConditionOutcome {
            return ConditionOutcome(false, message)
        }

        @JvmStatic
        fun noMatch(message: ConditionMessage): ConditionOutcome {
            return ConditionOutcome(false, message)
        }
    }
}