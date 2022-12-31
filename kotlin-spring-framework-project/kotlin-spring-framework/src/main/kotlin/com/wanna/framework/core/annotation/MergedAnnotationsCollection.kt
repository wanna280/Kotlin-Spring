package com.wanna.framework.core.annotation

import java.util.function.Predicate

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
open class MergedAnnotationsCollection(private val annotations: Array<MergedAnnotation<*>>) : MergedAnnotations {

    override fun <A : Annotation> isPresent(annotationType: Class<A>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPresent(annotationName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> isDirectPresent(annotationType: Class<A>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDirectPresent(annotationName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(annotationType: Class<A>): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>,
        predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(
        annotationType: Class<A>,
        predicate: Predicate<in MergedAnnotation<A>>?,
        selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(annotationName: String): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(
        annotationName: String,
        predicate: Predicate<in MergedAnnotation<A>>?
    ): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation> get(
        annotationName: String,
        predicate: Predicate<in MergedAnnotation<A>>?,
        selector: MergedAnnotationSelector<A>?
    ): MergedAnnotation<A> {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun of(annotations: Array<MergedAnnotation<*>>): MergedAnnotations {
            return MergedAnnotationsCollection(annotations)
        }
    }
}