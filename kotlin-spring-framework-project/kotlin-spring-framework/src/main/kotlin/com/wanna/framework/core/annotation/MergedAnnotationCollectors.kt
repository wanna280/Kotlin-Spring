package com.wanna.framework.core.annotation

import com.wanna.framework.core.annotation.MergedAnnotation.Adapt
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import java.lang.UnsupportedOperationException
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collector.Characteristics

/**
 * MergedAnnotation的Collectors工具类, 主要是提供对于注解的Stream流的元素收集
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
object MergedAnnotationCollectors {

    /**
     * 无Characteristics的常量
     */
    private val NO_CHARACTERISTICS = emptyArray<Characteristics>()

    /**
     * IDENTITY_FINISH_CHARACTERISTICS的常量
     */
    private val IDENTITY_FINISH_CHARACTERISTICS = arrayOf(Characteristics.IDENTITY_FINISH)


    /**
     * 将Stream<MergedAnnotation>收集成为Set<Annotation>的Collector
     *
     * @param A 要去进行收集的注解类型
     * @return 提供元素的收集的Collector
     */
    @JvmStatic
    fun <A : Annotation> toAnnotationSet(): Collector<in MergedAnnotation<A>, out Any, out Set<A>> {
        return Collector.of(Supplier { LinkedHashSet() }, { set, ann -> set.add(ann.synthesize()) }, this::combiner)
    }

    /**
     * 将给定的Stream<MergedAnnotation>收集成为MultiValueMap的Collector
     *
     * @param finisher 完成之后需要执行的操作
     * @param adapts 是否需要将注解属性当中的Value转为String, Annotation转为Map?
     * @return Collector
     */
    @JvmStatic
    fun <A : Annotation> toMultiValueMap(
        finisher: Function<MultiValueMap<String, Any>, MultiValueMap<String, Any>>, vararg adapts: Adapt
    ): Collector<in MergedAnnotation<A>, out Any, out MultiValueMap<String, Any>> {
        val characteristics =
            if (finisher == Function.identity<MultiValueMap<String, Any>>()) NO_CHARACTERISTICS else IDENTITY_FINISH_CHARACTERISTICS
        return Collector.of(
            { LinkedMultiValueMap() },  // 创建最终结果的Supplier
            { map, ann ->
                ann.asMap(adapts = adapts).forEach(map::add)
            },  // 将一个MergedAnnotation转为Map并添加到MultiValueMap当中
            this::combiner,  // 联合两个MultiValueMap当中的元素
            finisher,  // 当执行完之后, 需要去进行收集的最终操作
            *characteristics
        )
    }

    /**
     * 联合两个集合当中的元素
     *
     * @param collection collection
     * @param additional 要去进行额外添加的元素
     * @return 往collection当中添加完元素之后的结果
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun <E, C : Collection<E>> combiner(collection: C, additional: C): C {
        if (collection !is MutableCollection<*>) {
            throw UnsupportedOperationException("Unsupported to use immutable collection to combine")
        }
        (collection as MutableCollection<E>).addAll(additional)
        return collection
    }

    /**
     * 聚合两个MultiValueMap当中的元素
     *
     * @param K Key类型
     * @param V Value类型
     * @param M Map类型
     * @param map map
     * @param additional 额外要去添加的元素
     * @return 往map当中添加完元素之后的结果
     */
    @JvmStatic
    private fun <K, V, M : MultiValueMap<K, V>> combiner(map: M, additional: M): M {
        map.putAll(additional)
        return map
    }
}