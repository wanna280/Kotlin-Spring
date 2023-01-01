package com.wanna.framework.context.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.InvocationTargetException

/**
 * 给定注解的相关信息的工具类
 *
 * @see AnnotationAttributes
 */
object AnnotationAttributesUtils {
    /**
     * 获取目标注解的相关属性，包装到AnnotationAttributes对象当中
     * 方便后续进行获取对应的属性，通过很简单的方法就可以获取到
     *
     * @param annotation 目标注解
     * @return 目标注解的属性的包装对象AnnotationAttributes对象当中
     */
    @Suppress("UNCHECKED_CAST")
    @Nullable
    @JvmStatic
    fun asAnnotationAttributes(@Nullable annotation: Annotation?): AnnotationAttributes? {
        annotation ?: return null

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

            // 如果是注解的话, 那么递归去进行转换...
            if (attrValue is Annotation) {
                attrValue = asAnnotationAttributes(attrValue)

                // 如果是一个注解数组的话, 那么转换成为Array<AnnotationAttributes>
            } else if (attrValue != null && attrValue::class.java.isArray && attrValue::class.java.componentType.isAnnotation) {
                val arrayOfAnnotations = attrValue as Array<Annotation>
                val annotationArray =
                    java.lang.reflect.Array.newInstance(
                        AnnotationAttributes::class.java,
                        arrayOfAnnotations.size
                    ) as Array<AnnotationAttributes?>
                arrayOfAnnotations.indices.forEach {
                    // 递归转换
                    annotationArray[it] = asAnnotationAttributes(arrayOfAnnotations[it])
                }
            }
            // 添加到Attributes当中去
            attributes[attrKey] = attrValue!!
        }
        return attributes
    }

    /**
     * 将给定的注解去转换成为一个非空的AnnotationAttributes
     *
     * @param annotation 注解
     */
    @JvmStatic
    fun asNonNullAnnotationAttributes(annotation: Annotation): AnnotationAttributes {
        return asAnnotationAttributes(annotation) ?: throw IllegalStateException("AnnotationAttributes is null")
    }

    /**
     * 给定一个指定的注解列表，将其包装成为一个属性集合
     *
     * @param annotations 目标注解列表
     */
    @JvmStatic
    fun asAnnotationAttributesSet(vararg annotations: Annotation): Set<AnnotationAttributes> =
        annotations.mapNotNull { asAnnotationAttributes(it) }.toSet()

    /**
     * 给定一个指定的注解列表，将其包装成为一个属性集合
     *
     * @param annotations 目标注解列表
     */
    @JvmStatic
    fun asAnnotationAttributesSet(annotations: Collection<*>): Set<AnnotationAttributes> =
        annotations.filterIsInstance<Annotation>().mapNotNull { asAnnotationAttributes(it) }.toSet()

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