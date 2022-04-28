package com.wanna.framework.beans.annotations

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个Spring当中的Bean，被标注在一个配置类的方法上
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Bean(
    @get:AliasFor("name")
    val value: String = "",
    @get:AliasFor("value")
    val name: String = "",

    val initMethod: String = "",
    val destoryMethod: String = ""
)
