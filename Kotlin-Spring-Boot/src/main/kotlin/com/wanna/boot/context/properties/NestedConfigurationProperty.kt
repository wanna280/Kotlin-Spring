package com.wanna.boot.context.properties

/**
 * 在@ConfigurationProperties的Bean去进行绑定时，如果需要绑定的字段，并不是一个简单类型，
 * 而是一个复杂的对象类型，使用这个注解去标注，就可以保证内部的字段都可以被当做@ConfigurationProperties去进行绑定
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
