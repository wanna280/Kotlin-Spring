package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * 根据MergedAnnotation列表去构建MergedAnnotations
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @param annotations MergedAnnotation列表
 */
open class MergedAnnotationsCollection(private val annotations: Array<MergedAnnotation<*>>) : MergedAnnotations {

    /**
     * Mappings
     */
    private val mappings: Array<AnnotationTypeMappings> = Array(annotations.size) {
        AnnotationTypeMappings.forAnnotationType(annotations[it].type)
    }

    override fun <A : Annotation> isPresent(annotationType: Class<A>) = isPresent(annotationType, false)

    override fun isPresent(annotationName: String) = isPresent(annotationName, false)

    override fun <A : Annotation> isDirectPresent(annotationType: Class<A>) = isPresent(annotationType, true)

    override fun isDirectPresent(annotationName: String) = isPresent(annotationName, true)

    override fun <A : Annotation> get(annotationType: Class<A>): MergedAnnotation<A> {
        return get(annotationType, null, null)
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>, predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        return get(annotationType, predicate, null)
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>, predicate: Predicate<in MergedAnnotation<A>>?, selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        return find(annotationType, predicate, selector) ?: MergedAnnotation.missing()
    }

    override fun <A : Annotation> get(annotationName: String): MergedAnnotation<A> {
        return get(annotationName, null, null)
    }

    override fun <A : Annotation> get(
        annotationName: String, predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        return get(annotationName, predicate, null)
    }

    override fun <A : Annotation> get(
        annotationName: String, predicate: Predicate<in MergedAnnotation<A>>?, selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        return find(annotationName, predicate, selector) ?: MergedAnnotation.missing()
    }

    @Suppress("UNCHECKED_CAST")
    override fun stream(): Stream<MergedAnnotation<Annotation>> =
        this.annotations.toList().stream() as Stream<MergedAnnotation<Annotation>>

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<MergedAnnotation<Annotation>> =
        this.annotations.iterator() as Iterator<MergedAnnotation<Annotation>>

    /**
     * 检查是否存在有指定的注解?
     */
    private fun isPresent(requiredType: Any, directOnly: Boolean): Boolean {
        // 先检查直接标注的情况
        for (annotation in annotations) {
            if (annotation.type == requiredType || annotation.type.name == requiredType) {
                return true
            }
        }

        // 如果不只是检查直接标注的情况, 那么进行递归检查...
        if (!directOnly) {
            for (typeMappings in mappings) {
                for (i in 0 until typeMappings.size) {
                    val mapping = typeMappings[i]
                    if (isMappingForType(mapping, requiredType)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Annotation> find(
        requiredType: Any,
        @Nullable predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A>? {
        val selectorToUse = selector ?: MergedAnnotationSelectors.nearest()

        // result
        var result: MergedAnnotation<A>? = null
        for (index in this.annotations.indices) {
            val root = annotations[index]
            val typeMappings = mappings[index]

            for (i in 0 until typeMappings.size) {
                val mapping = typeMappings[i]
                if (!isMappingForType(mapping, requiredType)) {
                    continue
                }
                val candidate =
                    if (i == 0) root as MergedAnnotation<A> else TypeMappedAnnotation.createIfPossible<A>(mapping, root)
                if (candidate != null && (predicate == null || predicate.test(candidate))) {
                    if (selectorToUse.isBestCandidate(candidate)) {
                        return candidate
                    }
                    result = if (result != null) selectorToUse.select(result, candidate) else candidate
                }
            }
        }
        return result
    }

    companion object {
        @JvmStatic
        fun of(annotations: Array<MergedAnnotation<*>>): MergedAnnotations {
            return MergedAnnotationsCollection(annotations)
        }

        @JvmStatic
        private fun isMappingForType(mapping: AnnotationTypeMapping, @Nullable requiredType: Any?): Boolean {
            requiredType ?: return true
            val annotationType = mapping.annotationType
            return requiredType == annotationType || annotationType.name == requiredType
        }
    }
}