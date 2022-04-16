package com.wanna.framework.beans.factory.support

/**
 * 这是一个支持处理Context相关的注解，比如Lazy注解的AutowireCandidateResolver
 */
class ContextAnnotationAutowireCandidateResolver : QualifierAnnotationAutowireCandidateResolver() {

    companion object {
        @JvmField
        val INSTANCE = ContextAnnotationAutowireCandidateResolver()
    }

}