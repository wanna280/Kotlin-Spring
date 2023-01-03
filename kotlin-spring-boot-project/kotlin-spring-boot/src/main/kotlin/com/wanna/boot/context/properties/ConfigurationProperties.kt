package com.wanna.boot.context.properties

import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个ConfigurationProperties，它会自动匹配环境当中的属性去对Bean当中的属性去进行属性的填充；
 * 对于内部要去进行填充的字段是一个复杂的对象时，可以使用`@NestedConfigurationProperty`注解去进行实现；
 * 需要注意的是，要使用@ConfigurationProperties注解，得需要先使用@EnableConfigurationProperties去开启
 * 相关的属性绑定的功能的相关的支持
 *
 * 它可以标注在：
 * * 1.SpringBean的配置类上
 * * 2.SpringBean配置类的@Bean方法上
 *
 * @see NestedConfigurationProperty
 * @see EnableConfigurationProperties
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ConfigurationProperties(
    @get:AliasFor("value")
    val prefix: String = "",
    @get:AliasFor("prefix")
    val value: String = ""
)
