package com.wanna.framework.core.type

import com.wanna.framework.core.annotation.*
import com.wanna.framework.core.annotation.MergedAnnotation.Adapt.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.MultiValueMap
import java.util.Collections
import java.util.function.Function

/**
 * 这是一个被注解标注的的类型的Metadata信息，支持去获取到注解的相关属性；
 * 主要有两类AnnotatedTypeMetadata的实现，一类是方法的Metadata(MethodMetadata)，另外一类是类的Metadata(AnnotationMetadata)；
 *
 * @see MethodMetadata
 * @see AnnotationMetadata
 */
interface AnnotatedTypeMetadata {

    /**
     * 获取MergedAnnotations
     *
     * @return MergedAnnotations
     */
    fun getAnnotations(): MergedAnnotations

    /**
     * 判断该类型上是否标注了某个注解？(支持直接标注/以Meta注解的方式去进行标注)
     *
     * @param annotationName 注解的全类名
     * @return 如果有标注的话，那么return true；否则return false
     */
    fun isAnnotated(annotationName: String): Boolean = getAnnotations().isPresent(annotationName)

    /**
     * 指定具体的注解类型的className，去寻找到合适的注解的对应属性(对于注解对象, 将会转换成为Map; 对于Class的属性值, 不会转换为String)
     *
     * @param annotationName 注解类型的全类名
     * @return 为该注解去解析到的注解属性Map; 如果不存在该注解的话, return null
     */
    @Nullable
    fun getAnnotationAttributes(annotationName: String): Map<String, Any>? =
        getAnnotationAttributes(annotationName, false)

    /**
     * 获取指定的AnnotationType对应的注解的AnnotationAttributes(对于注解对象, 将会转换成为Map)
     *
     * @param annotationName 要去获取的注解ClassName
     * @param classValueAsString 是否需要将值为Class的属性值去转换为String?
     * @return 为该注解去解析到的注解属性Map; 如果不存在该注解的话, return null
     */
    @Nullable
    fun getAnnotationAttributes(annotationName: String, classValueAsString: Boolean): Map<String, Any>? {
        val mergedAnnotation =
            getAnnotations().get(annotationName, null, MergedAnnotationSelectors.firstDirectlyDeclared())

        // 如果注解不存在的话, return null
        if (!mergedAnnotation.present) {
            return null
        }
        // 如果存在的话, 将该注解去转换为字符串, 并且如果遇到了注解当中有注解的话, 那么注解对象将会被转换成为Map
        return mergedAnnotation.asAnnotationAttributes(*MergedAnnotation.Adapt.values(classValueAsString, true))
    }

    /**
     * 指定具体的注解clazz，去寻找到合适的注解的对应属性
     *
     * @param annotationClass 注解的类型
     * @return 解析到的注解属性Map; 如果不存在该注解的话，那么return null
     */
    @Nullable
    fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any>? =
        getAnnotationAttributes(annotationClass.name)

    /**
     * 指定具体的注解类型的className，去寻找到**所有**合适的注解的对应属性(对于注解对象, 将会转换成为Map; 对于Class的属性值, 不会转换为String)
     *
     * @param annotationName 注解类型的全类名
     * @return 为该注解去解析到的注解属性Map; 如果不存在该注解的话, return null
     */
    @Nullable
    fun getAllAnnotationAttributes(annotationName: String): MultiValueMap<String, Any>? =
        getAllAnnotationAttributes(annotationName, false)


    /**
     * 指定具体的注解类型的className，去寻找到**所有**合适的注解的对应属性(对于注解对象, 将会转换成为Map)
     *
     * @param annotationName 注解类型的全类名
     * @param classValueAsString 是否需要将值为Class的属性值去转换为String?
     * @return 为该注解去解析到的注解属性Map; 如果不存在该注解的话, return null
     */
    @Nullable
    fun getAllAnnotationAttributes(annotationName: String, classValueAsString: Boolean): MultiValueMap<String, Any>? {
        val adapts = MergedAnnotation.Adapt.values(classValueAsString, true)
        return getAnnotations()
            .stream<Annotation>(annotationName)
            .map(MergedAnnotation<Annotation>::withNonMergedAttributes)
            .collect(
                MergedAnnotationCollectors.toMultiValueMap(
                    Function<MultiValueMap<String, Any>, MultiValueMap<String, Any>> {
                        return@Function if (it.isEmpty()) null else it
                    }, *adapts
                )
            )
    }
}