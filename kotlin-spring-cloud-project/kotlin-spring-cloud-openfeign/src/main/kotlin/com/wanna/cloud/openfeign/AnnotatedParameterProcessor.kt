package com.wanna.cloud.openfeign

import feign.MethodMetadata
import java.lang.reflect.Method

/**
 * OpenFeign的注解的参数处理器
 */
interface AnnotatedParameterProcessor {

    /**
     * 获取它支持处理的注解类型(只能支持一个注解类型, 不支持多个注解类型)
     */
    fun getAnnotationType(): Class<out Annotation>

    /**
     * 解析目标参数, 会遍历目标方法参数上的所有注解, 去循环调用这个方法完成参数的处理
     *
     * @param context context
     * @param annotation 目标方法参数上的一个注解
     * @param method 目标方法
     */
    fun processArgument(context: AnnotatedParameterContext, annotation: Annotation, method: Method): Boolean

    /**
     * 一个注解参数的解析的Context, 协助去完成参数的解析
     */
    interface AnnotatedParameterContext {
        fun getMethodMetadata(): MethodMetadata

        fun getParameterIndex(): Int

        fun setParameterName(name: String)

        fun setTemplateParameter(name: String, rest: Collection<String>?): Collection<String>
    }
}