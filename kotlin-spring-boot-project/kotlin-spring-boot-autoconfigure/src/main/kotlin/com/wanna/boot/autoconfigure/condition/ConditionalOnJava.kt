package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.condition.ConditionalOnJava.Range
import com.wanna.framework.context.annotation.Conditional

/**
 * 只有在Java版本符合给定的要求时, 才需要去进行自动装配
 *
 * @see JavaVersion
 * @see Range
 * @see OnJavaCondition
 *
 * @param range 要匹配的Java版本的方式, ">="/"<"
 * @param value 要匹配的Java版本( EIGHT("8") / NIGHT("9") / TEN("10") )
 */
@Conditional(OnJavaCondition::class)
annotation class ConditionalOnJava(val value: JavaVersion, val range: Range = Range.EQUAL_OR_NEWER) {
    enum class Range(val rangeName: String) {
        /**
         * ">="
         */
        EQUAL_OR_NEWER(">="),

        /**
         * "<"
         */
        OLDER_THAN("<")
    }
}
