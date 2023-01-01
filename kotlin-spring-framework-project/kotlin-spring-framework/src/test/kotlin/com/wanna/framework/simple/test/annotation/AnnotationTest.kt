package com.wanna.framework.simple.test.annotation

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.*

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
@Configuration(value = "wanna")
class AnnotationTest1 {

}

/**
 * 测试Annotation, 检查能否通过Meta注解去获取到source注解当中的属性信息
 */
fun testAnnotation1() {
    val annotations = MergedAnnotations.from(AnnotationTest1::class.java)
    val componentAnnotation = annotations.get(Component::class.java)
    val name = componentAnnotation.getValue("value").get()
    println("annotation1 name is $name")
}

fun testAnnotation2() {
    val typeMappings = AnnotationTypeMappings.forAnnotationType(Configuration::class.java)
    println("annotation2 $typeMappings")
}


annotation class MirrorAnn(
    @get:AliasFor("value")
    val name: String = "",
    @get:AliasFor("name")
    val value: String = ""
)

annotation class MirrorAnn2(
    @get:AliasFor("value", annotation = MirrorAnn::class)
    val name: String = "",
    @get:AliasFor("name", annotation = MirrorAnn::class)
    val value: String = ""
)


@MirrorAnn(value = "wanna1")
class AnnotationTest2 {

}


/**
 * 验证一下mirror的情况能否正常识别? 直接通过直接注解去进行配置
 */
fun testAnnotation3() {
    val annotations = MergedAnnotations.from(AnnotationTest2::class.java)
    val componentMergedAnnotation = annotations.get(MirrorAnn::class.java)
    println("annotation3 name is ${componentMergedAnnotation.getValue("name").get()}")
    println("annotation3 value is ${componentMergedAnnotation.getValue("value").get()}")
}

@MirrorAnn2(value = "wanna3")
class AnnotationTest3 {

}

/**
 * 验证一下mirror的情况能否正常识别? 通过source去进行配置
 */
fun testAnnotation4() {
    val annotations = MergedAnnotations.from(AnnotationTest2::class.java)
    val componentMergedAnnotation = annotations.get(MirrorAnn::class.java)
    println("annotation4 name is ${componentMergedAnnotation.getValue("name").get()}")
    println("annotation4 value is ${componentMergedAnnotation.getValue("value").get()}")
}

/**
 * 测试合成注解能否正常使用
 */
fun testAnnotation5() {
    val annotations = MergedAnnotations.from(AnnotationTest2::class.java)
    val annotation = annotations.get(MirrorAnn::class.java)
    val synthesize = annotation.synthesize()
    println("annotation5 name is " + synthesize.name)
    println("annotation5 value is " + synthesize.value)
    println("annotation5 hashCode is " + synthesize.hashCode())
    println(synthesize)
}


fun main() {
    testAnnotation1()
    testAnnotation2()
    testAnnotation3()
    testAnnotation4()
    testAnnotation5()


}