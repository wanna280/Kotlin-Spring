package com.wanna.cloud.openfeign.support

import com.wanna.cloud.openfeign.AnnotatedParameterProcessor
import com.wanna.cloud.openfeign.AnnotatedParameterProcessor.AnnotatedParameterContext
import com.wanna.cloud.openfeign.FeignUtils
import com.wanna.cloud.openfeign.annotation.RequestBodyParameterProcessor
import com.wanna.cloud.openfeign.annotation.RequestHeaderParameterProcessor
import com.wanna.cloud.openfeign.annotation.RequestParamParameterProcessor
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.web.bind.annotation.RequestMapping
import feign.Feign
import feign.MethodMetadata
import feign.Request.HttpMethod.*
import java.lang.reflect.Method

/**
 * SpringMvc的Contract, 主要是解析以支持SpringMvc相关的注解, 将其适配到Feign当中;
 *
 * 默认情况下, Feign支持的是解析Feign当中的@RequestLine/@Headers/@Param/@QueryMap/@HeaderMap/@Body这些注解;
 * 我们在Spring当中, 想要直接使用SpringMvc相关的注解, 因此, 我们在这里去进行自定义注解的处理逻辑, 而不是仅仅使用Feign默认的Contract！
 *
 * @see AnnotatedParameterProcessor
 */
open class SpringMvcContract(
    processors: MutableList<AnnotatedParameterProcessor>, private val conversionService: FormattingConversionService
) : feign.Contract.BaseContract() {

    companion object {
        private val PARAMETER_NAME_DISCOVERER = DefaultParameterNameDiscoverer()  // 参数名发现器
    }

    // 参数处理器列表(key-注解类型, value-该注解类型对应的注解处理器)
    private val argumentsProcessors: MutableMap<Class<out Annotation>, AnnotatedParameterProcessor> = HashMap()

    // 已经处理过的方法列表
    private val processedMethods = HashMap<String, Method>()

    init {
        val annotatedParameterProcessors = ArrayList(getDefaultAnnotatedArgumentsProcessors())
        annotatedParameterProcessors += processors
        annotatedParameterProcessors.forEach { argumentsProcessors[it.getAnnotationType()] = it }
    }

    /**
     * Feign, 会根据这个方法的返回值, 去进行HandlerMethod的构建?
     *
     * @param targetType FeignClient Interface
     */
    override fun parseAndValidateMetadata(targetType: Class<*>?): MutableList<MethodMetadata> {
        return super.parseAndValidateMetadata(targetType)
    }

    override fun processAnnotationOnClass(data: MethodMetadata?, clz: Class<*>?) {

    }

    /**
     * 处理一个方法上的注解(这里主要解析@RequestMapping注解)
     *
     * @param data methodMetadata
     * @param annotation 注解
     * @param method 要处理的方法
     */
    override fun processAnnotationOnMethod(data: MethodMetadata, annotation: Annotation, method: Method) {
        // 添加方法缓存, 方便后期拿出来(似乎没有必要存在? 因为MethodMetadata当中可以获取到方法? )
        processedMethods[Feign.configKey(method.declaringClass, method)] = method
        val requestMapping = AnnotatedElementUtils.getMergedAnnotation(method, RequestMapping::class.java)
        if (requestMapping != null) {
            // 解析请求方式的情况...
            val requestMethods = requestMapping.method
            val requestMethod = valueOf(if (requestMethods.isNotEmpty()) requestMethods[0].name.uppercase() else "GET")
            data.template().method(requestMethod)  // 必须指定请求方式...

            // 解析路径的情况...
            val paths = requestMapping.path
            val pathToUse = if (paths.isEmpty()) "/" else paths[0]
            data.template().uri(pathToUse)
        }
    }

    /**
     * 处理一个方法的其中一个参数上的全部注解(至于是哪个参数, 通过paramIndex去进行确定)
     *
     * @param data methodMetadata
     * @param annotations 某个方法参数当中的注解列表?
     * @param paramIndex 该方法参数位于方法当中的索引index?
     * @return 该参数是否是一个Http注解(如果为true时, 可以使用nameParam(MethodMetadata data,String name,int i)获取到该参数的值)
     */
    override fun processAnnotationsOnParameter(
        data: MethodMetadata, annotations: Array<out Annotation>, paramIndex: Int
    ): Boolean {
        var isHttpAnnotation = false
        val method = processedMethods[data.configKey()]
        val context = SimpleAnnotatedParameterContext(data, paramIndex)

        // 遍历所有的方法参数当中的注解列表, 挨个去进行判断, 看是否有合适的处理器去处理该注解?
        annotations.forEach {
            val processor = argumentsProcessors[it.annotationClass.java]
            if (processor != null) {
                if (processor.processArgument(context, it, method!!)) {
                    isHttpAnnotation = true
                }
            }
        }
        return isHttpAnnotation
    }

    /**
     * 获取默认的参数处理器列表
     */
    private fun getDefaultAnnotatedArgumentsProcessors(): List<AnnotatedParameterProcessor> {
        val processors = ArrayList<AnnotatedParameterProcessor>()
        processors += RequestParamParameterProcessor()
        processors += RequestHeaderParameterProcessor()
        processors += RequestBodyParameterProcessor()
        return processors
    }

    /**
     * AnnotatedParameterContext的简单实现
     */
    private inner class SimpleAnnotatedParameterContext(
        private val metadata: MethodMetadata, private val paramIndex: Int
    ) : AnnotatedParameterContext {
        override fun getMethodMetadata() = metadata
        override fun getParameterIndex() = paramIndex
        override fun setParameterName(name: String) {
            this@SpringMvcContract.nameParam(metadata, name, paramIndex)
        }

        override fun setTemplateParameter(name: String, rest: Collection<String>?): Collection<String> {
            return FeignUtils.addTemplateParameter(rest, name)
        }
    }
}