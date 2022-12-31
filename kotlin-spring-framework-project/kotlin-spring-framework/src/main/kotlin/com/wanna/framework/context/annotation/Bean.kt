package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个Spring当中的Bean，被标注在一个配置类的方法上，
 * 使用案例如下：
 *
 * ```kotlin
 *     @Bean
 *     open fun user(): User {
 *         return User()
 *     }
 * ```
 *
 * Note: @Bean方法支持在方法参数处，去提供Spring的依赖的自动注入的功能，对于@Bean方法的Bean创建，
 * 其实本质上属于构造器注入的方式，因此@Bean方法当中如果出现了循环依赖，那么无解(因为Bean都没完成创建放入到三级缓存当中)
 *
 * @param value beanName
 * @param name beanName
 * @param autowireMode 自动注入模式
 * @param autowireCandidate 是否是AutowireCandidate
 * @param initMethod initMethod
 * @param destroyMethod destroyMethod
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Bean(
    @get:com.wanna.framework.core.annotation.AliasFor("name")
    @get:AliasFor("name")
    val value: String = "",
    @get:AliasFor("value")
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    val name: String = "",
    val autowireCandidate: Boolean = true,  // 是否是AutowireCandidate？
    val autowireMode: Int = AutowireCapableBeanFactory.AUTOWIRE_NO,  // AutowireMode
    val initMethod: String = "",
    val destroyMethod: String = ""
)
