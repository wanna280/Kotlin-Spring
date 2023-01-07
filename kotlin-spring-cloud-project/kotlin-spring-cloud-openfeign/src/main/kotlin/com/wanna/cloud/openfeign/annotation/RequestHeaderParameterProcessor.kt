package com.wanna.cloud.openfeign.annotation

import com.wanna.cloud.openfeign.AnnotatedParameterProcessor
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.annotation.RequestHeader
import java.lang.reflect.Method

/**
 * 基于@RequestHeader的注解参数处理器
 */
open class RequestHeaderParameterProcessor : AnnotatedParameterProcessor {

    companion object {
        private val ANNOTATION = RequestHeader::class.java
    }

    override fun getAnnotationType() = ANNOTATION

    override fun processArgument(
        context: AnnotatedParameterProcessor.AnnotatedParameterContext,
        annotation: Annotation,
        method: Method
    ): Boolean {
        val type = method.parameterTypes[context.getParameterIndex()]
        val data = context.getMethodMetadata()

        // 如果参数是@RequestHeader + type=Map
        if (ClassUtils.isAssignFrom(Map::class.java, type)) {
            data.headerMapIndex(context.getParameterIndex())
            return true
        }
        val requestHeader = ANNOTATION.cast(annotation)
        val name = requestHeader.value
        // 设置paramName(添加到indexToName当中)
        context.setParameterName(name)

        // 添加模板参数...后期它会从indexToName当中去进行搜索并替换
        val query = context.setTemplateParameter(name, data.template().headers()[name])
        data.template().header(name, query)
        return true
    }
}