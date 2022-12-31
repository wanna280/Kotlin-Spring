package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
object AnnotationsScanner {

    @JvmStatic
    fun <C, R> scan(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        val result = process(context, source, searchStrategy, processor)
        return processor.finish(result)
    }

    @JvmStatic
    private fun <C, R> process(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (source) {
            is Class<*> -> processClass(context, source, searchStrategy, processor)
            is Method -> processMethod(context, source, searchStrategy, processor)
            else -> processElement(context, source, searchStrategy, processor)
        }
    }

    @JvmStatic
    private fun <C, R> processClass(
        context: C,
        source: Class<*>,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {
            MergedAnnotations.SearchStrategy.DIRECT -> processElement(context, source, searchStrategy, processor)
            else -> null
        }
    }

    @JvmStatic
    private fun <C, R> processMethod(
        context: C,
        source: Method,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return when (searchStrategy) {
            MergedAnnotations.SearchStrategy.DIRECT -> processElement(context, source, searchStrategy, processor)
            else -> null
        }
    }

    /**
     * 处理一个AnnotatedElement上的注解
     */
    @JvmStatic
    private fun <C, R> processElement(
        context: C,
        source: AnnotatedElement,
        searchStrategy: MergedAnnotations.SearchStrategy,
        processor: AnnotationsProcessor<C, R>
    ): R? {
        return processor.doWithAggregate(context, 0) ?: processor.doWithAnnotations(
            context, 0, source, getDeclaredAnnotations(source, false)
        )
    }

    /**
     * 获取到目标元素身上的所有的直接定义的注解
     *
     * @param source source
     * @param defensive 是否具有侵入性?
     * @return declaredAnnotations
     */
    @JvmStatic
    fun getDeclaredAnnotations(source: AnnotatedElement, defensive: Boolean): Array<Annotation> {
        return source.declaredAnnotations
    }

    /**
     * 从给定的source上去找到AnnotationType类型的注解
     *
     * @param source 要去进行寻找注解的元素
     * @param annotationType 要去进行寻找的注解类型
     * @param A  要去进行寻找的注解类型
     * @return 从source上去找到的AnnotationType的注解(找不到return null)
     */
    @JvmStatic
    @Nullable
    @Suppress("UNCHECKED_CAST")
    fun <A : Annotation> getDeclaredAnnotation(source: AnnotatedElement, annotationType: Class<A>): A? {
        val declaredAnnotations = getDeclaredAnnotations(source, false)
        for (annotation in declaredAnnotations) {
            if (annotation.annotationClass.java == annotationType) {
                return annotation as A
            }
        }
        return null
    }
}