package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport

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

    override fun stream(): Stream<MergedAnnotation<Annotation>> {
        return StreamSupport.stream(spliterator(), false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> stream(annotationType: Class<A>): Stream<MergedAnnotation<A>> {
        return StreamSupport.stream(spliterator(annotationType), false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> stream(annotationName: String): Stream<MergedAnnotation<A>> {
        return StreamSupport.stream(spliterator(annotationName), false)
    }

    override fun iterator(): Iterator<MergedAnnotation<Annotation>> {
        return Spliterators.iterator(spliterator())
    }

    override fun spliterator(): Spliterator<MergedAnnotation<Annotation>> {
        return spliterator(null)
    }


    /**
     * 获取到MergedAnnotation的Spliterator
     *
     * @param annotationType 需要的注解类型(null代表要全部类型)
     * @return Spliterator of MergedAnnotation
     */
    private fun <A : Annotation> spliterator(@Nullable annotationType: Any?): Spliterator<MergedAnnotation<A>> {
        return AnnotationsSpliterator(annotationType)
    }

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
                    if (i == 0) root as MergedAnnotation<A> else TypeMappedAnnotation.createIfPossible(mapping, root)
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

    /**
     * AnnotationSpliterator
     *
     * @param requiredType 要去进行遍历的注解类型(null代表遍历所有)
     */
    private inner class AnnotationsSpliterator<A : Annotation>(@Nullable private val requiredType: Any?) :
        Spliterator<MergedAnnotation<A>> {

        /**
         * mappingCursor, 记录AnnotationIndex对应的AnnotationTypeMappings迭代到的位置
         */
        private val mappingCursors = IntArray(annotations.size)
        override fun tryAdvance(action: Consumer<in MergedAnnotation<A>>): Boolean {
            var lowestDistance = Int.MAX_VALUE
            var annotationResult = -1
            for (annotationIndex in annotations.indices) {
                val mapping: AnnotationTypeMapping? = getNextSuitableMapping(annotationIndex)
                if (mapping != null && mapping.distance < lowestDistance) {
                    annotationResult = annotationIndex
                    lowestDistance = mapping.distance
                }
                if (lowestDistance == 0) {
                    break
                }
            }
            if (annotationResult != -1) {
                val mergedAnnotation =
                    createMergedAnnotationIfPossible(annotationResult, mappingCursors[annotationResult])
                mappingCursors[annotationResult]++
                if (mergedAnnotation == null) {
                    return tryAdvance(action)
                }
                action.accept(mergedAnnotation)
                return true
            }
            return false
        }

        /**
         * 获取下一个位置的合适的AnnotationTypeMapping
         *
         * @param annotationIndex annotationIndex
         * @return AnnotationTypeMapping(如果没有合适的了, return null)
         */
        @Nullable
        private fun getNextSuitableMapping(annotationIndex: Int): AnnotationTypeMapping? {
            var mapping: AnnotationTypeMapping?
            do {
                mapping = getMapping(annotationIndex, mappingCursors[annotationIndex])
                if (mapping != null && isMappingForType(mapping, requiredType)) {
                    return mapping
                }
                mappingCursors[annotationIndex]++
            } while (mapping != null)
            return null
        }

        /**
         * 根据AnnotationIndex和MappingIndex, 去获取到对应位置的AnnotationTypeMapping
         *
         * @param annotationIndex annotationIndex
         * @param mappingIndex mappingIndex
         * @return AnnotationTypeMapping(mappingIndex越界return null)
         */
        @Nullable
        private fun getMapping(annotationIndex: Int, mappingIndex: Int): AnnotationTypeMapping? {
            val typeMappings = mappings[annotationIndex]
            return if (mappingIndex < typeMappings.size) typeMappings[mappingIndex] else null
        }

        /**
         * 如果必要的话, 创建一个MergedAnnotation
         *
         * @param annotationIndex annotationIndex
         * @param mappingIndex mappingIndex
         * @return MergedAnnotation
         */
        @Suppress("UNCHECKED_CAST")
        @Nullable
        private fun createMergedAnnotationIfPossible(annotationIndex: Int, mappingIndex: Int): MergedAnnotation<A>? {
            val root = annotations[annotationIndex]
            if (mappingIndex == 0) {
                return root as MergedAnnotation<A>
            }
            return TypeMappedAnnotation.createIfPossible(mappings[annotationIndex][mappingIndex], root)
        }

        @Nullable
        override fun trySplit(): Spliterator<MergedAnnotation<A>>? = null

        override fun estimateSize(): Long {
            var size = 0
            for (i in annotations.indices) {
                val mappings: AnnotationTypeMappings = mappings[i]
                var numberOfMappings = mappings.size
                numberOfMappings -= mappingCursors[i].coerceAtMost(mappings.size)
                size += numberOfMappings
            }
            return size.toLong()
        }

        override fun characteristics(): Int = Spliterator.NONNULL or Spliterator.IMMUTABLE
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