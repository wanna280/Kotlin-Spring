package com.wanna.framework.core.annotation

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/10
 */
annotation class Config(@get:AliasFor("value") val prefix: String = "", @get:AliasFor("prefix") val value: String = "")

@Config("config")
class AnnotationTest

fun main() {
    val mergedAnnotations = MergedAnnotations.from(AnnotationTest::class.java)
    val mergedAnnotation = mergedAnnotations.get(Config::class.java)
    val config = mergedAnnotation.synthesize()

    assert(config.prefix == config.value && config.value.isNotEmpty())
}