package com.wanna.cloud.openfeign.annotation

import com.wanna.cloud.openfeign.AnnotatedParameterProcessor
import com.wanna.framework.web.bind.annotation.RequestBody
import java.lang.reflect.Method

/**
 * 基于@RequestBody的参数处理器
 */
open class RequestBodyParameterProcessor : AnnotatedParameterProcessor {
    companion object {
        private val ANNOTATION = RequestBody::class.java
    }

    override fun getAnnotationType() = ANNOTATION

    override fun processArgument(
        context: AnnotatedParameterProcessor.AnnotatedParameterContext,
        annotation: Annotation,
        method: Method
    ): Boolean {
        val data = context.getMethodMetadata()
        data.bodyIndex(context.getParameterIndex())
        return true
    }
}