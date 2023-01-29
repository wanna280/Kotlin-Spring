package com.wanna.framework.context.annotation

/**
 * FilterType类型枚举
 */
enum class FilterType {
    ANNOTATION,  // 匹配注解
    ASSIGNABLE_TYPE,  // 匹配类型
    CUSTOM,  // 自定义类型
}