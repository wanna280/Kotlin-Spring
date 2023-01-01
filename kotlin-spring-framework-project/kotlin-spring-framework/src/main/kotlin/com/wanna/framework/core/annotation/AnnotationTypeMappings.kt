package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.util.*
import kotlin.collections.ArrayList

/**
 * 描述了一个注解对应的所有的间接Meta注解的描述信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @param repeatableContainers 重复注解的Container
 * @param annotationType 要去进行描述的注解的类型
 * @param annotationFilter 对于该注解当中的MetaAnnotation来说, 哪些是需要被过滤的?
 */
class AnnotationTypeMappings(
    private val repeatableContainers: RepeatableContainers,
    private val annotationType: Class<out Annotation>,
    private val annotationFilter: AnnotationFilter,
) {

    /**
     * 一个注解当中所有的间接Meta注解的映射信息, 从AnnotationType开始去进行递归的添加, 遍历方式为BFS
     */
    private val mappings = ArrayList<AnnotationTypeMapping>()

    init {
        // 从AnnotationType开始去进行递归处理, 从而构建出来所有的Mapping
        addAllMappings(annotationType)
    }

    /**
     * 根据index去获取到对应位置的AnnotationTypeMapping
     *
     * @param index index(0代表root注解)
     * @return AnnotationTypeMapping
     */
    operator fun get(index: Int): AnnotationTypeMapping = mappings[index]

    /**
     * 获取到当前注解对应的Mapping的总数量
     *
     * @return mapping size
     */
    val size: Int
        get() = this.mappings.size


    /**
     * 从给定的AnnotationType开始, 去递归构建出来所有的Meta注解的相关信息
     *
     * @param annotationType root AnnotationType
     */
    private fun addAllMappings(annotationType: Class<out Annotation>) {
        val queue = ArrayDeque<AnnotationTypeMapping>()
        // 先将root注解去添加到队列当中, 对于root注解来说, source=null&ann=null
        addIfPossible(queue, null, annotationType, null)

        // 使用BFS广度优先遍历的方式, 将所有的递归的Meta注解全部进行处理, 并添加到mappings当中
        while (queue.isNotEmpty()) {
            val mapping = queue.removeFirst()
            mappings += mapping

            // 将当前注解的全部Meta注解都添加到队列当中去...
            addMetaAnnotationsToQueue(queue, mapping)
        }
    }

    /**
     * 将给定的source的注解的所有元注解添加到队列当中
     *
     * @param queue queue
     * @param source source(描述了一个注解的相关信息)
     */
    private fun addMetaAnnotationsToQueue(queue: Deque<AnnotationTypeMapping>, source: AnnotationTypeMapping) {
        // 获取到source对应的注解的所有的直接Meta注解
        val declaredAnnotations = AnnotationsScanner.getDeclaredAnnotations(source.annotationType, false)

        declaredAnnotations.forEach {
            if (!isMappable(source, it)) {
                return@forEach
            }
            // 如果存在有重复注解的Conatiner, 那么尝试处理重复注解的情况
            val repeatedAnnotations = repeatableContainers.findRepeatedAnnotations(it)
            if (repeatedAnnotations != null) {
                for (index in repeatedAnnotations.indices) {
                    val repeatAnnotation = repeatedAnnotations[index]
                    if (!isMappable(source, repeatAnnotation)) {
                        continue
                    }
                    addIfPossible(queue, source, repeatAnnotation)
                }
            } else {
                // 如果该Meta注解符合AnnotationFilter的要求的话, 那么加入到队列当中
                addIfPossible(queue, source, it)
            }
        }
    }

    /**
     * 检查这个注解是否需要我们去进行映射?
     *
     * @param source source注解信息
     * @param metaAnnotation Meta注解
     * @return 如果它能被AnnotationFilter匹配的话, 那么return true; 否则return false
     */
    private fun isMappable(source: AnnotationTypeMapping, @Nullable metaAnnotation: Annotation?): Boolean {
        return metaAnnotation != null && !this.annotationFilter.matches(metaAnnotation)  // check metaAnnotation
                && !AnnotationFilter.PLAIN.matches(source.annotationType)  // check source
    }

    private fun addIfPossible(
        queue: Deque<AnnotationTypeMapping>, @Nullable source: AnnotationTypeMapping?, ann: Annotation
    ) {
        addIfPossible(queue, source, ann.annotationClass.java, ann)
    }

    private fun addIfPossible(
        queue: Deque<AnnotationTypeMapping>,
        @Nullable source: AnnotationTypeMapping?,
        annotationType: Class<out Annotation>,
        @Nullable ann: Annotation?
    ) {
        queue.add(AnnotationTypeMapping(source, annotationType, ann))
    }


    companion object {

        /**
         * 根据一个注解类型, 去建立起来该注解对应的映射信息
         *
         * @param annotationType 注解类型
         * @return 注解的映射信息
         */
        @JvmStatic
        fun forAnnotationType(annotationType: Class<out Annotation>): AnnotationTypeMappings {
            return forAnnotationType(annotationType, AnnotationFilter.PLAIN)
        }

        /**
         * 根据一个注解类型, 去建立起来该注解对应的映射信息
         *
         * @param annotationType 注解类型
         * @param annotationFilter 要去过滤注解的Filter
         * @return 注解的映射信息
         */
        @JvmStatic
        fun forAnnotationType(
            annotationType: Class<out Annotation>,
            annotationFilter: AnnotationFilter,
        ): AnnotationTypeMappings {
            return forAnnotationType(annotationType, annotationFilter, RepeatableContainers.standardRepeatables())
        }

        /**
         * 根据一个注解类型, 去建立起来该注解对应的映射信息
         *
         * @param annotationType 注解类型
         * @param annotationFilter 要去过滤注解的Filter
         * @param repeatableContainers 重复注解的寻找的Container
         * @return 注解的映射信息
         */
        @JvmStatic
        fun forAnnotationType(
            annotationType: Class<out Annotation>,
            annotationFilter: AnnotationFilter,
            repeatableContainers: RepeatableContainers
        ): AnnotationTypeMappings {
            return AnnotationTypeMappings(repeatableContainers, annotationType, annotationFilter)
        }

        @JvmStatic
        fun clearCache() {

        }
    }
}