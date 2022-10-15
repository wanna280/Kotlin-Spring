package com.wanna.framework.core.annotation

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
class TypeMappedAnnotation<A : Annotation> : MergedAnnotation<A> {

    override fun getType(): Class<A> {
        TODO("Not yet implemented")
    }
}