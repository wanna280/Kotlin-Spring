package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
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
    val autowireCandidate: Boolean = true,  // 是否是AutowireCandidate？
    val autowireMode: Int = AutowireCapableBeanFactory.AUTOWIRE_NO,  // AutowireMode
    val initMethod: String = "",
    val destoryMethod: String = ""
)
