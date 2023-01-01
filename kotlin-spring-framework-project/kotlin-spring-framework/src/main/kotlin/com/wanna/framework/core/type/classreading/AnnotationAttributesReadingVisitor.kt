package com.wanna.framework.core.type.classreading

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.util.MultiValueMap

/**
 * 提供注解的属性的读取的AnnotationVisitor, 需要支持递归注解的读取, 因为对于一个注解来说, 它的属性当中也支持去配置成为一个注解
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
open class AnnotationAttributesReadingVisitor(
    annotationType: String,
    private val attributesMap: MultiValueMap<String, AnnotationAttributes>,
    private val metaAnnotationMap: MutableMap<String, Set<String>>,
    classLoader: ClassLoader
) : RecursiveAnnotationAttributesVisitor(
    annotationType,
    classLoader,
    AnnotationAttributes(annotationType, classLoader)
) {

    /**
     * 在访问结束时, 将attributes收集到AttributesMap当中
     */
    override fun visitEnd() {
        val annotationClass = this.attributes.annotationType ?: return

        val list = attributesMap[annotationType]
        if (list == null) {
            attributesMap.add(annotationType, attributes)
        } else {
            list.add(0, attributes)
        }
        val annotations = annotationClass.annotations
        val visited = LinkedHashSet<Annotation>()
        annotations.forEach {
            if (!it.annotationClass.java.name.startsWith("java.lang.annotation")) {
                visited += it
            }
        }
        val metaAnnotationTypeNames = LinkedHashSet<String>(visited.size)
        visited.forEach { metaAnnotationTypeNames += it.annotationClass.java.name }
        metaAnnotationMap[annotationClass.name] = metaAnnotationTypeNames
    }
}