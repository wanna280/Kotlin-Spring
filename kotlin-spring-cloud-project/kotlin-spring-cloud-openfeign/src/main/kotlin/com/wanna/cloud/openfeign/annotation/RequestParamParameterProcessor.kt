package com.wanna.cloud.openfeign.annotation

import com.wanna.cloud.openfeign.AnnotatedParameterProcessor
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.annotation.RequestParam
import java.lang.reflect.Method

/**
 * 基于@RequestParam的注解参数处理器
 */
open class RequestParamParameterProcessor : AnnotatedParameterProcessor {
    companion object {
        private val ANNOTATION = RequestParam::class.java
    }

    override fun getAnnotationType() = ANNOTATION

    override fun processArgument(
        context: AnnotatedParameterProcessor.AnnotatedParameterContext,
        annotation: Annotation,
        method: Method
    ): Boolean {
        val data = context.getMethodMetadata()
        val type = method.parameterTypes[context.getParameterIndex()]

        // 如果它是@RequestParam + type=Map的话, 说明它是对应Feign的queryMap
        if (ClassUtils.isAssignFrom(Map::class.java, type)) {
            data.queryMapIndex(context.getParameterIndex())
            return true
        }
        // 如果type!=Map, 说明它只是一个普通的query参数值
        val requestParam = ANNOTATION.cast(annotation)
        val name = requestParam.name
        context.setParameterName(name)

        // 添加模板参数...重写构建query, 并设置到data.template当中
        val query = context.setTemplateParameter(name, data.template().queries()[name])
        data.template().query(name, query)
        return true
    }
}