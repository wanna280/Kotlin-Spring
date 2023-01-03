package com.wanna.framework.simple.test.annotation

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.stereotype.Service
import com.wanna.framework.core.annotation.*
import com.wanna.framework.core.type.GlobalTypeSwitch
import com.wanna.framework.core.type.classreading.SimpleMetadataReaderFactory

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
    @get:AliasFor("value") val name: String = "", @get:AliasFor("name") val value: String = ""
)

annotation class MirrorAnn2(
    @get:AliasFor("value", annotation = MirrorAnn::class) val name: String = "",
    @get:AliasFor("name", annotation = MirrorAnn::class) val value: String = ""
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

@Service("666")
@Configuration("777")
@Component("888")
class AnnotationTest4

fun testAnnotation6() {
    val allMergedAnnotations =
        AnnotatedElementUtils.getAllMergedAnnotations(AnnotationTest4::class.java, Component::class.java)
    println("annotation6 component size is" + allMergedAnnotations.size)

    val mergedAnnotation = MergedAnnotations.from(AnnotationTest4::class.java).get(Component::class.java)
    val defaultValue = mergedAnnotation.getDefaultValue("value")
    println("annotation6 default value is ${defaultValue.get()}")
}

annotation class Ann1(val value: String)

annotation class Ann2(val value: Array<Ann1>)

@Ann2([Ann1("1"), Ann1("2")])
class AnnotationTest5

/**
 * 验证MergedAnnotation内部的MergedAnnotation的读取
 */
fun testAnnotation7() {
    val metadataReaderFactory = SimpleMetadataReaderFactory()
    val metadataReader = metadataReaderFactory.getMetadataReader(AnnotationTest5::class.java.name)
    val ann2MergedAnnotation = metadataReader.annotationMetadata.getAnnotations().get(Ann2::class.java)
    val value = ann2MergedAnnotation.getValue("value", Array<Ann1>::class.java)
    println("annotation7 is $value")
}

fun testAnnotation8() {
    val metadataReaderFactory = SimpleMetadataReaderFactory()
    val metadataReader = metadataReaderFactory.getMetadataReader(AnnotationTest5::class.java.name)
    val ann2MergedAnnotation = metadataReader.annotationMetadata.getAnnotations().get(Ann2::class.java)
    val value = ann2MergedAnnotation.asAnnotationAttributes()
    val toMapValue = ann2MergedAnnotation.asAnnotationAttributes(MergedAnnotation.Adapt.ANNOTATION_TO_MAP)
    println("annotation8 origin attributes is $value")
    println("annotation8 to map attributes is $toMapValue")

}

/**
 * 测试AnnotationUtils
 */
fun testAnnotation9() {
    val metadataReaderFactory = SimpleMetadataReaderFactory()
    val metadataReader = metadataReaderFactory.getMetadataReader(AnnotationTest5::class.java.name)
    val ann2MergedAnnotation = metadataReader.annotationMetadata.getAnnotations().get(Ann2::class.java)
    val attributes = AnnotationUtils.getAnnotationAttributes(ann2MergedAnnotation.synthesize())
    println("annotation9 is $attributes")
}


fun main() {
    GlobalTypeSwitch.annotatedElementUtilsOpen = true

    testAnnotation1()
    testAnnotation2()
    testAnnotation3()
    testAnnotation4()
    testAnnotation5()
    testAnnotation6()
    testAnnotation7()
    testAnnotation8()
    testAnnotation9()

}