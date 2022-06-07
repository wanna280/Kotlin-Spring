package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.InvocationTargetException

/**
 * 给定注解的相关信息的工具类
 */
object AnnotationAttributesUtils {
    /**
     * 获取目标注解的相关属性，包装到AnnotationAttributes对象当中
     * 方便后续进行获取对应的属性，通过很简单的方法就可以获取到
     *
     * @param annotation 目标注解
     * @return 目标注解的属性的包装对象AnnotationAttributes对象当中
     */
    @JvmStatic
    fun asAnnotationAttributes(annotation: Annotation?): AnnotationAttributes? {
        if (annotation == null) {
            return null
        }
        val annotationType: Class<out Annotation> = annotation.annotationClass.java
        val attributes = AnnotationAttributes(annotationType)
        // 获取到目标注解对应的全部方法，将其解析出来放到AnnotationAttributes中去
        val methods = annotationType.declaredMethods
        for (method in methods) {
            val attrKey = method.name
            var attrValue: Any? = null
            try {
                attrValue = method.invoke(annotation)
            } catch (e: IllegalAccessException) {
                // ignore，不可能出现这种情况...
            } catch (e: InvocationTargetException) {
            }
            attributes[attrKey] = attrValue!!
        }
        return attributes
    }

    /**
     * 给定一个指定的注解列表，将其包装成为一个属性集合
     *
     * @param annotations 目标注解列表
     */
    @JvmStatic
    fun asAnnotationAttributesSet(vararg annotations: Annotation?): Set<AnnotationAttributes?> {
        val attributesSet: MutableSet<AnnotationAttributes?> = HashSet()
        for (annotation in annotations) {
            attributesSet.add(asAnnotationAttributes(annotation))
        }
        return attributesSet
    }

    /**
     * 给定一个指定的注解列表，将其包装成为一个属性集合
     *
     * @param annotations 目标注解列表
     */
    @JvmStatic
    fun asAnnotationAttributesSet(annotations: Collection<*>): Set<AnnotationAttributes> {
        return annotations.filterIsInstance<Annotation?>().mapNotNull { asAnnotationAttributes(it) }.toSet()
    }

    /**
     * 从一个Map转换到Attributes对象
     *
     * @param map map
     * @return AnnotationAttributes对象
     */
    @JvmStatic
    fun fromMap(map: Map<String, Any>): AnnotationAttributes {
        val attributes = AnnotationAttributes()
        attributes.putAll(map)
        return attributes
    }
}