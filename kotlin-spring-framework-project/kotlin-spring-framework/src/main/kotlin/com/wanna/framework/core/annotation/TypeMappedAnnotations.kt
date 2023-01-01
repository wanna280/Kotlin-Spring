package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * 针对一个具体的类型(类/方法/字段/构造器等), 去进行描述的MergedAnnotations
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param annotationFilter 要去进行过滤的注解Filter
 * @param annotations 注解列表
 * @param element 要去进行描述的目标元素(类/方法/字段)
 * @param searchStrategy 搜索注解的策略
 * @param repeatableContainers 重复注解的Container
 */
open class TypeMappedAnnotations(
    private val annotationFilter: AnnotationFilter,
    @Nullable private val annotations: Array<Annotation>? = null,
    @Nullable private val element: AnnotatedElement? = null,
    @Nullable private val searchStrategy: MergedAnnotations.SearchStrategy? = null,
    private val repeatableContainers: RepeatableContainers?
) : MergedAnnotations {

    @Nullable
    private var aggregates: List<Aggregate>? = null


    //---------------------------------------检查注解是否存在的相关API开始-----------------------------------
    /**
     * 检查MergedAnnotations当中是否存在有给定的注解?
     *
     * @param annotationType annotationType
     * @return 如果存在有给定的注解的话, return true; 否则return false
     */
    override fun <A : Annotation> isPresent(annotationType: Class<A>): Boolean {
        return !this.annotationFilter.matches(annotationType) && true == scan(
            annotationType, IsPresent.get(null, annotationFilter, false)
        )
    }

    /**
     * 检查MergedAnnotations当中是否存在有给定的注解name的注解?
     *
     * @param annotationName annotationName
     * @return 如果存在有给定的注解的话, return true; 否则return false
     */
    override fun isPresent(annotationName: String): Boolean {
        return !this.annotationFilter.matches(annotationName) && true == scan(
            annotationName, IsPresent.get(null, annotationFilter, false)
        )
    }

    /**
     * 检查MergedAnnotations当中是否直接标注有给定的注解?
     *
     * @param annotationType 要去进行检查的注解类型
     * @return 如果直接标注的话, 那么return true; 否则return false
     */
    override fun <A : Annotation> isDirectPresent(annotationType: Class<A>): Boolean {
        return !this.annotationFilter.matches(annotationType) && true == scan(
            annotationType, IsPresent.get(null, annotationFilter, true)
        )
    }

    /**
     * 检查MergedAnnotations当中是否直接标注有给定的注解?
     *
     * @param annotationName annotationName
     * @return 如果直接标注的话, 那么return true; 否则return false
     */
    override fun isDirectPresent(annotationName: String): Boolean {
        return !this.annotationFilter.matches(annotationName) && true == scan(
            annotationName, IsPresent.get(null, annotationFilter, true)
        )
    }
    //---------------------------------------检查注解是否存在的相关API结束-----------------------------------

    //---------------------------------------提供去获取注解的相关API开始-----------------------------------

    override fun <A : Annotation> get(annotationType: Class<A>): MergedAnnotation<A> {
        return get(annotationType, null, null)
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>, @Nullable predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        return get(annotationType, predicate, null)
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>,
        @Nullable predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        if (this.annotationFilter.matches(annotationType)) {
            return MergedAnnotation.missing()
        }
        val result = scan(annotationType, MergedAnnotationFinder<A>(annotationType, predicate, selector))
        return result ?: MergedAnnotation.missing()
    }

    override fun <A : Annotation> get(annotationName: String): MergedAnnotation<A> {
        return get(annotationName, null, null)
    }

    override fun <A : Annotation> get(
        annotationName: String, @Nullable predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        return get(annotationName, null, null)
    }

    override fun <A : Annotation> get(
        annotationName: String,
        @Nullable predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        if (this.annotationFilter.matches(annotationName)) {
            return MergedAnnotation.missing()
        }
        val result = scan(annotationName, MergedAnnotationFinder<A>(annotationName, predicate, selector))
        return result ?: MergedAnnotation.missing()
    }

    //---------------------------------------提供去获取注解的相关API结束-----------------------------------

    override fun stream(): Stream<MergedAnnotation<Annotation>> {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Stream.empty()
        }
        return StreamSupport.stream(spliterator(), false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> stream(annotationType: Class<A>): Stream<MergedAnnotation<A>> {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Stream.empty()
        }
        return StreamSupport.stream(spliterator(annotationType), false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> stream(annotationName: String): Stream<MergedAnnotation<A>> {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Stream.empty()
        }
        return StreamSupport.stream(spliterator(annotationName), false)
    }

    override fun iterator(): Iterator<MergedAnnotation<Annotation>> {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Collections.emptyIterator()
        }
        return Spliterators.iterator(spliterator())
    }

    override fun spliterator(): Spliterator<MergedAnnotation<Annotation>> {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Spliterators.emptySpliterator()
        }
        return spliterator(null)
    }


    /**
     * 获取到MergedAnnotation的Spliterator
     *
     * @param annotationType 需要的注解类型(null代表要全部类型)
     * @return Spliterator of MergedAnnotation
     */
    private fun <A : Annotation> spliterator(@Nullable annotationType: Any?): Spliterator<MergedAnnotation<A>> {
        return AggregatesSpliterator(annotationType, getAggregates())
    }

    /**
     * 获取到所有的聚合元素
     *
     * @return Aggregates
     */
    private fun getAggregates(): List<Aggregate> {
        var aggregates: List<Aggregate>? = this.aggregates
        if (aggregates == null) {
            aggregates = scan(this, AggregateCollector())
            if (aggregates.isNullOrEmpty()) {
                aggregates = Collections.emptyList()
            }
            this.aggregates = aggregates
        }
        return aggregates!!
    }

    /**
     * 利用AnnotationsProcessor, 去执行处理
     *
     * @param criteria AnnotationType(AnnotationClassName)
     * @param processor AnnotationsProcessor
     */
    @Nullable
    private fun <C, R> scan(criteria: C, processor: AnnotationsProcessor<C, R>): R? {
        // 如果存在有直接给定的注解的话, 那么直接根据AnnotationProcessor从注解上去进行搜索即可
        if (this.annotations != null) {
            val result = processor.doWithAnnotations(criteria, 0, null, this.annotations)
            return processor.finish(result)
        }

        // 如果没有直接给定的注解的话, 那么需要根据AnnotatedElement去进行搜索
        if (this.element != null && this.searchStrategy != null) {
            return AnnotationsScanner.scan(criteria, element, searchStrategy, processor)
        }
        return null
    }

    /**
     * 聚合注解的Spliterator
     *
     * @param requiredType 需要的注解类型
     * @param aggregates 聚合注解列表
     */
    private inner class AggregatesSpliterator<A : Annotation>(
        @Nullable val requiredType: Any?, val aggregates: List<Aggregate>
    ) : Spliterator<MergedAnnotation<A>> {

        /**
         * Aggregate迭代的游标
         */
        private var aggregateCursor = 0

        /**
         * MappingCursors, 记录的是AnnotationIndex对应的MappingIndex的迭代index
         */
        @Nullable
        private var mappingCursors: IntArray? = null

        /**
         * 如果有, 则遍历下一个元素
         */
        override fun tryAdvance(action: Consumer<in MergedAnnotation<A>>): Boolean {
            while (aggregateCursor < aggregates.size) {
                val aggregate = this.aggregates[aggregateCursor]
                if (tryAdvance(aggregate, action)) {
                    return true
                }
                this.mappingCursors = null
                aggregateCursor++
            }
            return false
        }

        private fun tryAdvance(aggregate: Aggregate, action: Consumer<in MergedAnnotation<A>>): Boolean {
            // 初始化一个size=annotationIndex的数组
            if (this.mappingCursors == null) {
                this.mappingCursors = IntArray(aggregate.size)
            }
            var lowestDistance = Int.MAX_VALUE
            var annotationResult = -1
            for (annotationIndex in 0 until aggregate.size) {
                val mapping = getNextSuitableMapping(aggregate, annotationIndex)
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
                    aggregate.createIfPossible<A>(annotationResult, mappingCursors!![annotationResult])
                this.mappingCursors!![annotationResult]++
                if (mergedAnnotation == null) {
                    return tryAdvance(aggregate, action)
                }
                action.accept(mergedAnnotation)
                return true
            }
            return false
        }

        /**
         * 获取到下一个合适的AnnotationTypeMapping
         *
         * @param aggregate aggregate
         * @param annotationIndex annotationIndex
         */
        @Nullable
        private fun getNextSuitableMapping(aggregate: Aggregate, annotationIndex: Int): AnnotationTypeMapping? {
            val cursors = this.mappingCursors
            if (cursors != null) {
                var mapping: AnnotationTypeMapping?
                do {
                    mapping = aggregate.getMapping(annotationIndex, cursors[annotationIndex])
                    if (mapping != null && isMappingForType(mapping, annotationFilter, requiredType)) {
                        return mapping
                    }
                    cursors[annotationIndex]++
                } while (mapping != null)
            }
            return null
        }

        override fun trySplit(): Spliterator<MergedAnnotation<A>>? = null

        override fun estimateSize(): Long {
            var size = 0
            for (aggregateIndex in aggregateCursor until this.aggregates.size) {
                val aggregate: Aggregate = this.aggregates[aggregateIndex]
                for (annotationIndex in 0 until aggregate.size) {
                    val mappings = aggregate.getMappings(annotationIndex)
                    var numberOfMappings = mappings.size
                    if (aggregateIndex == aggregateCursor && mappingCursors != null) {
                        numberOfMappings -= Math.min(mappingCursors!![annotationIndex], mappings.size)
                    }
                    size += numberOfMappings
                }
            }
            return size.toLong()
        }

        override fun characteristics(): Int = Spliterator.NONNULL or Spliterator.IMMUTABLE
    }

    private class Aggregate(
        private val aggregateIndex: Int, private val source: Any?, private val annotations: List<Annotation>
    ) {

        /**
         * 为每个注解, 去构建出来AnnotationTypeMappings映射信息
         */
        private val mappings: Array<AnnotationTypeMappings> = Array(annotations.size) {
            AnnotationTypeMappings.forAnnotationType(annotations[it].annotationClass.java)
        }

        /**
         *
         * Annotation Size
         */
        val size: Int
            get() = this.annotations.size

        /**
         * 根据annotationIndex和mappingIndex去获取到该位置的AnnotationTypeMapping
         *
         * @param annotationIndex annotationIndex
         * @param mappingIndex mappingIndex
         * @return 该位置的AnnotationTypeMapping(如果mappingIndex越界return null)
         */
        @Nullable
        fun getMapping(annotationIndex: Int, mappingIndex: Int): AnnotationTypeMapping? {
            val typeMappings = mappings[annotationIndex]
            return if (mappingIndex < typeMappings.size) typeMappings[mappingIndex] else null
        }

        /**
         * 根据index去获取到对应位置的AnnotationTypeMappings
         *
         * @param annotationIndex index
         * @return AnnotationTypeMappings
         */
        fun getMappings(annotationIndex: Int): AnnotationTypeMappings = mappings[annotationIndex]

        /**
         * 根据annotationIndex和mappingIndex, 去创建出来MergedAnnotation
         *
         * @param annotationIndex annotationIndex
         * @param mappingIndex mappingIndex
         * @return 创建出来的MergedAnnotation对象
         */
        @Nullable
        fun <A : Annotation> createIfPossible(annotationIndex: Int, mappingIndex: Int): MergedAnnotation<A>? {
            return TypeMappedAnnotation.createIfPossible<A>(
                this.mappings[annotationIndex][mappingIndex],
                this.source,
                this.annotations[annotationIndex],
                this.aggregateIndex
            )
        }

    }

    /**
     * 聚合注解的收集器, 用于去将所有的注解去收集起来
     */
    private inner class AggregateCollector : AnnotationsProcessor<Any, List<Aggregate>> {

        private val aggregates = ArrayList<Aggregate>()

        @Nullable
        override fun doWithAnnotations(
            context: Any, aggregateIndex: Int, source: Any?, annotations: Array<Annotation>
        ): List<Aggregate>? {
            aggregates.add(createAggregate(aggregateIndex, source, annotations))
            return null
        }

        private fun createAggregate(aggregateIndex: Int, source: Any?, annotations: Array<Annotation>): Aggregate {
            return Aggregate(aggregateIndex, source, getAggregateAnnotations(annotations))
        }

        private fun getAggregateAnnotations(annotations: Array<Annotation>): List<Annotation> {
            val aggregateAnnotations = ArrayList<Annotation>()
            addAggregateAnnotations(aggregateAnnotations, annotations)
            return aggregateAnnotations
        }

        private fun addAggregateAnnotations(
            aggregateAnnotations: MutableList<Annotation>, annotations: Array<Annotation>
        ) {
            for (annotation in annotations) {
                if (!annotationFilter.matches(annotation)) {
                    aggregateAnnotations.add(annotation)
                }
            }

        }

        override fun finish(result: List<Aggregate>?): List<Aggregate> = this.aggregates
    }

    /**
     * 检查给定的注解是否存在的AnnotationProcessor
     *
     * @param annotationFilter AnnotationFilter
     * @param directOnly 是否只检查直接标注的注解?
     */
    private class IsPresent(
        private val repeatableContainers: RepeatableContainers?,
        private val annotationFilter: AnnotationFilter,
        private val directOnly: Boolean
    ) : AnnotationsProcessor<Any, Boolean> {

        /**
         * 对给定的所有的注解, 去执行检查是否其中标注了requiredType这个注解
         *
         * @param context requiredType(可以是Class, 也可以是ClassName)
         * @param annotations 待检查的注解列表
         * @return 如果给定的注解当中存在有requiredType的话return true, 否则return null
         */
        override fun doWithAnnotations(
            context: Any, aggregateIndex: Int, source: Any?, annotations: Array<Annotation>
        ): Boolean? {

            // 根据给定的所有的注解, 尝试去进行匹配
            for (annotation in annotations) {
                val annotationType = annotation.annotationClass.java

                // 检查这个注解类型是否应该跳过, 对于AnnotationFilter匹配上的, 不应该进行检查
                if (!this.annotationFilter.matches(annotationType)) {
                    // 如果context和AnnotationType匹配的话, return true
                    if (context == annotationType || context == annotationType.name) {
                        return true
                    }

                    // 如果不只是简单检查直接标注的情况的话, 那么还需要去检查递归Meta注解
                    if (!directOnly) {
                        val typeMappings = AnnotationTypeMappings.forAnnotationType(annotationType)
                        for (i in 0 until typeMappings.size) {

                            // 如果该mapping(元注解)和给定注解类型匹配的话, return true
                            if (isMappingForType(typeMappings[i], annotationFilter, context)) {
                                return true
                            }
                        }

                    }

                }
            }
            return null
        }

        companion object {
            /**
             * 对外提供静态工厂方法, 去构建出来一个检查目标注解是否存在的AnnotationsProcessor
             *
             * @param repeatableContainers RepeatableContainer
             * @param annotationFilter 执行注解的过滤的Filter
             * @param directOnly 是否只去检查直接标注的注解?
             * @return 用于去提供检查是否标注了指定的注解的AnnotationsProcessor
             */
            @JvmStatic
            fun get(
                repeatableContainers: RepeatableContainers?, annotationFilter: AnnotationFilter, directOnly: Boolean
            ): IsPresent {
                return IsPresent(repeatableContainers, annotationFilter, directOnly)
            }
        }
    }

    /**
     * MergedAnnotation的Finder
     *
     * @param requiredType 需要的注解类型
     * @param predicate 执行对于MergedAnnotation去进行匹配的断言, 只有在符合断言的情况下, 该结果才是我们需要的
     * @param selector 当遇到多个匹配的注解(MergedAnnotation)的话, 应该选取哪个去作为最终的结果的Selector
     */
    private inner class MergedAnnotationFinder<A : Annotation>(
        private val requiredType: Any?,
        @Nullable private val predicate: Predicate<in MergedAnnotation<A>>?,
        @Nullable selector: MergedAnnotationSelector<A>?
    ) : AnnotationsProcessor<Any, MergedAnnotation<A>> {

        /**
         * MergedAnnotationSelector
         */
        private val selector: MergedAnnotationSelector<A> = selector ?: MergedAnnotationSelectors.nearest()

        /**
         * 最终的结果的MergedAnnotation
         */
        private var result: MergedAnnotation<A>? = null

        override fun doWithAggregate(context: Any, aggregateIndex: Int): MergedAnnotation<A>? = this.result

        override fun doWithAnnotations(
            context: Any, aggregateIndex: Int, source: Any?, annotations: Array<Annotation>
        ): MergedAnnotation<A>? {
            for (annotation in annotations) {
                if (!annotationFilter.matches(annotation)) {
                    val result = process(context, aggregateIndex, source, annotation)
                    if (result != null) {
                        return result
                    }
                }
            }
            return null
        }

        /**
         * 对单个注解上的元注解去进行处理
         */
        private fun process(
            type: Any, aggregateIndex: Int, source: Any?, annotation: Annotation
        ): MergedAnnotation<A>? {
            // 建立起来给定的注解的注解的映射关系
            val mappings = AnnotationTypeMappings.forAnnotationType(annotation.annotationClass.java, annotationFilter)

            for (i in 0 until mappings.size) {
                val mapping = mappings[i]
                if (isMappingForType(mapping, annotationFilter, this.requiredType)) {

                    // 为该注解实例去创建出来一个MergedAnnotation
                    val mergedAnnotation =
                        TypeMappedAnnotation.createIfPossible<A>(mapping, source, annotation, aggregateIndex)

                    // 更新lastResult
                    if (mergedAnnotation != null && (this.predicate == null || predicate.test(mergedAnnotation))) {
                        // 如果它已经是最好的选择了的话, 那么直接return
                        if (this.selector.isBestCandidate(mergedAnnotation)) {
                            return mergedAnnotation
                        }

                        // 如果它还不是最好的选择的话, 需要检查当前给定的MergedAnnotation是否是更好的选择?
                        updateLastResult(mergedAnnotation)
                    }
                }
            }

            return null
        }

        /**
         * 更新result
         */
        private fun updateLastResult(candidate: MergedAnnotation<A>) {
            val lastResult = this.result
            this.result = if (lastResult != null) this.selector.select(lastResult, candidate) else candidate
        }

        /**
         * 在最终完成时, 决定要使用哪个注解去作为最终的MergedAnnotation
         *
         * @param result result
         * @return 如果result不为空的话, 那么就使用它了; 如果它为空的话, 那么返回当前的AnnotationsSelector当中的保存的result
         */
        @Nullable
        override fun finish(@Nullable result: MergedAnnotation<A>?): MergedAnnotation<A>? = result ?: this.result
    }


    companion object {
        @JvmStatic
        private fun isMappingForType(
            mapping: AnnotationTypeMapping, annotationFilter: AnnotationFilter, requiredType: Any?
        ): Boolean {
            val annotationType = mapping.annotationType
            return !annotationFilter.matches(annotationType) && (requiredType == null || requiredType == annotationType || requiredType == annotationType.name)
        }

        @JvmStatic
        fun from(
            element: AnnotatedElement,
            searchStrategy: MergedAnnotations.SearchStrategy,
            repeatableContainers: RepeatableContainers?,
            annotationFilter: AnnotationFilter
        ): MergedAnnotations {
            return TypeMappedAnnotations(annotationFilter, null, element, searchStrategy, repeatableContainers)
        }

        @JvmStatic
        fun from(
            source: Any?,
            annotations: Array<Annotation>,
            repeatableContainers: RepeatableContainers?,
            annotationFilter: AnnotationFilter
        ): MergedAnnotations {
            return TypeMappedAnnotations(annotationFilter, annotations, null, null, repeatableContainers)
        }
    }

}