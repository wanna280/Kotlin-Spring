package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 扫描指定的包下的所有类下的标注了Component的组件
 */
@Repeatable
annotation class ComponentScan(
    @get:AliasFor("basePackages")
    val value: Array<String> = [],

    @get:AliasFor("value")
    val basePackages: Array<String> = [],
)
