package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
interface AnnotationsProcessor<C, R> {

    @Nullable
    fun doWithAggregate(context: C, aggregateIndex: Int): R? = null

    @Nullable
    fun doWithAnnotations(context: C, aggregateIndex: Int, @Nullable source: Any?, annotations: Array<Annotation?>): R?

    @Nullable
    fun finish(@Nullable result: R?): R? {
        return result
    }
}