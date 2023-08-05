package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.lang.Nullable

/**
 * 这是一个[SpringBootCondition]的匹配结果的一层抽象, Outcome(结果)-意思和Result基本一致,差不多可以通用
 *
 * @see SpringBootCondition
 *
 * @param match 匹配成功/失败
 * @param message 匹配的消息
 */
open class ConditionOutcome(val match: Boolean, val message: ConditionMessage) {

    /**
     * 提供一个基于普通的字符串message的构造器
     *
     * @param match 匹配成功/失败
     * @param message 匹配的消息
     */
    constructor(match: Boolean, @Nullable message: String?) : this(match, ConditionMessage.of(message))

    /**
     * 获取匹配的Message字符串
     *
     * @return message
     */
    open fun getMessageString(): String = this.message.toString()


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