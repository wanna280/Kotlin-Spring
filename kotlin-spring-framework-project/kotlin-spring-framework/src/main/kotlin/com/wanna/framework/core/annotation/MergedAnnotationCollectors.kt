package com.wanna.framework.core.annotation

import java.lang.UnsupportedOperationException
import java.util.stream.Collector

/**
 * MergedAnnotation的Collectors工具类, 主要是提供对于注解的Stream流的元素收集
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
object MergedAnnotationCollectors {

    /**
     * 将Stream<MergedAnnotation>收集成为Set<Annotation>的Collector
     *
     * @return 提供元素的收集的Collector
     * @param A 要去进行收集的注解类型
     */
    @JvmStatic
    fun <A : Annotation> toAnnotationSet(): Collector<in MergedAnnotation<A>, out Any, out Set<A>> {
        return Collector.of({ LinkedHashSet() }, { set, ann -> set.add(ann.synthesize()) }, this::combiner)
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
}