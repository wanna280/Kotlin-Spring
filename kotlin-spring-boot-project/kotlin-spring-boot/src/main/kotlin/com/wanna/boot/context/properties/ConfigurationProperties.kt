package com.wanna.boot.context.properties

import com.wanna.framework.core.annotation.AliasFor

/**
 * 标识这是一个[ConfigurationProperties]的Bean, 它会自动匹配环境当中的属性去对Bean当中的属性去对Bean进行属性的填充;
 * 需要注意的是, 要使用`@ConfigurationProperties`注解, 得需要先使用`@EnableConfigurationProperties`去开启
 * 相关的属性绑定的功能的相关的支持
 *
 * 它可以标注在:
 * * 1.SpringBean的配置类上
 * * 2.SpringBean配置类的`@Bean`方法上
 *
 * @see NestedConfigurationProperty
 * @see EnableConfigurationProperties
 *
 * @param prefix 要去进行绑定的属性前缀, 同value
 * @param value 要去进行绑定的属性前缀, 同prefix
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ConfigurationProperties(
    @get:AliasFor("prefix", annotation = ConfigurationProperties::class)
    val value: String = "",
    @get:AliasFor("value", annotation = ConfigurationProperties::class)
    val prefix: String = "",
)
