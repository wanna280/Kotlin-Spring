package com.wanna.framework.core.annotation


/**
 * MergedAnnotation Selector
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @see MergedAnnotationSelectors
 */
fun interface MergedAnnotationSelector<A : Annotation> {

    /**
     * 是否是一个最好的候选MergedAnnotation
     *
     * @param annotation annotation
     * @return 如果它是一个最好的MergedAnnotation, return true; 否则return false
     */
    fun <A : Annotation> isBestCandidate(annotation: MergedAnnotation<A>): Boolean = false

    /**
     * 从给定的两个MergedAnnotation当中去选取出来一个合适的MergedAnnotation
     *
     * @param existing 之前已经存在的MergedAnnotation
     * @param candidate 遇到的新的MergedAnnotation
     * @return 从两者当中选取出来的一个合适的MergedAnnotation
     */
    fun select(existing: MergedAnnotation<A>, candidate: MergedAnnotation<A>): MergedAnnotation<A>
}