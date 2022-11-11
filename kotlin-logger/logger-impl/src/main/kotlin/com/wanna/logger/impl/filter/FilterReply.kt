package com.wanna.logger.impl.filter

/**
 * 这是对Filter的决策结果的枚举，用来决定本次日志要不要去进行输出？
 *
 * (1)DENY，直接pass掉，不进行输出
 * (2)ACCEPT，不管日志级别是什么，都需要去进行输出
 * (3)NEUTRAL，只有日志级别符合要求的话，才会去进行输出
 */
enum class FilterReply {
    DENY,   // 拒绝
    NEUTRAL, // 中性
    ACCEPT  // 接受
}