package com.wanna.boot.context.properties

/**
 * 在@ConfigurationProperties的Bean去进行绑定时, 如果需要绑定的字段, 并不是一个简单类型,
 * 而是一个复杂的对象类型, 也支持去完成自动绑定, 这个注解只是为了AnnotationProcessor使用的
 *
 * 使用案例如下：
 *
 * ```kotlin
 * @ConfigurationProperties(prefix = "com.wanna")
 * class Properties {
 *     var prop: String? = null
 *
 *     @NestedConfigurationProperty
 *     var user: User? = null
 * }
 * ```
 *
 * @see ConfigurationProperties
 */
@Target(AnnotationTarget.FIELD)
annotation class NestedConfigurationProperty
