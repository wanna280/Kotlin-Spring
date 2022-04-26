package com.wanna.logger.impl.filter

/**
 * 这是对Filter的决策结果的枚举，用来决定本次日志要不要去进行输出？
 */
enum class FilterReply {
    DENY,   // 拒绝
    NEUTRAL, // 中性
    ACCEPT  // 接受
}