package com.wanna.framework.context.annotation

/**
 * 标识这个类需要去进行配置绑定
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class ConfigurationProperties(val prefix: String = "", val value: String = "", val name: String = "")
