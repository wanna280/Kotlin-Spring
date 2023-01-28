package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.context.EmbeddedValueResolverAware
import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.bind.annotation.CrossOrigin
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.RequestMappingInfoHandlerMapping
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 这是一个处理@RequestMapping注解的HandlerMapping, 基本上所有的功能都在它的父类当中实现了, 它需要实现相关的模板方法：
 * (1)如何判断它是一个Handler? ---如果类上标注了@RequestMapping/@Controller注解, 它就是一个支持当前类去进行处理的Handler
 * (2)如果解析一个HandlerMethod? ---解析方法/类上的@RequestMapping注解的相关信息即可判断它是否是一个HandlerMethod
 *
 * @see RequestMapping
 * @see Controller
 * @see RequestMappingInfoHandlerMapping
 */
open class RequestMappingHandlerMapping : RequestMappingInfoHandlerMapping(), EmbeddedValueResolverAware {

    /**
     * 嵌入式值解析器, 用于提供占位符的解析工作, 对RequestMapping当中配置的路径, 支持从SpringEnvironment当中去进行获取
     *
     * @see StringValueResolver
     */
    @Nullable
    private var embeddedValueResolver: StringValueResolver? = null

    /**
     * 内容协商管理器
     */
    private var contentNegotiationManager = ContentNegotiationManager()

    /**
     * 怎么判断它是否是一个Handler? 只需要类上加了@Controller/@RequestMapping注解, 它就是一个Handler;
     * 这里使用的hasAnnotation的API, 可以向目标类的父类当中去进行搜索; 对于一个Controller产生了代理的情况下, 这种情况是很必要的!!!
     *
     * @param beanType beanType
     * @return 它是否是一个Handler(如果标注了@Controller/@RequestMapping注解return true)
     */
    override fun isHandler(beanType: Class<*>): Boolean {
        return AnnotatedElementUtils.hasAnnotation(beanType, Controller::class.java) ||
                AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping::class.java)
    }

    /**
     * 设置嵌入式的值解析器
     *
     * @param resolver 你想要使用的嵌入式值解析器
     */
    override fun setEmbeddedValueResolver(resolver: StringValueResolver) {
        this.embeddedValueResolver = resolver
    }

    /**
     * 给定handlerMethod和handlerType, 返回Mapping([RequestMappingInfo]), 这里因为方法上和类上都有可能有@RequestMapping注解,
     * 因此, 我们需要去进行合并, 但是由于合并的算法不会写, 目前仅仅提供了路径的前缀功能, 别的功能算法不会写！！！
     *
     * @param method method
     * @param handlerType handlerType
     * @return 如果方法上找到了@RequestMapping注解, return封装好的[RequestMappingInfo]; 不然return null
     */
    @Nullable
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        // 从方法上找到@RequestMapping注解
        val info = getRequestMappingInfo(method)
        // 如果方法上找到了@RequestMapping注解, 那么需要尝试去类上去进行寻找
        if (info != null) {
            val typeInfo = getRequestMappingInfo(handlerType)
            // 如果类上也有@RequestMapping的话, 需要联合两个RequestMappingInfo去作为最终的RequestMappingInfo
            if (typeInfo != null) {
                return combine(typeInfo, info)
            }
        }
        return info
    }

    /**
     * 初始化CORS的配置信息, 我们需要处理@CrossOrigin注解
     *
     * @param handler handler
     * @param mapping mapping
     * @param method method
     * @return CorsConfiguration
     */
    @Nullable
    override fun initCorsConfiguration(handler: Any, method: Method, mapping: RequestMappingInfo): CorsConfiguration? {
        val handlerType = createHandlerMethod(handler, method).beanType!!
        val typeAnnotation = AnnotatedElementUtils.getMergedAnnotation(handlerType, CrossOrigin::class.java)
        val methodAnnotation = AnnotatedElementUtils.getMergedAnnotation(method, CrossOrigin::class.java)
        if (typeAnnotation == null && methodAnnotation == null) {
            return null
        }
        val corsConfig = CorsConfiguration()

        // 1. 如果类上有配置@CrossOrigin, 需要添加到CorsConfig
        updateCorsConfig(corsConfig, typeAnnotation)
        // 2. 如果方法上有@CrossOrigin, 那么需要继续添加配置信息
        updateCorsConfig(corsConfig, methodAnnotation)

        // if null,apply default to permit
        return corsConfig.applyPermitDefaultValues()
    }

    /**
     * 根据给定的@CrossOrigin注解, 去更新当前的Cors的配置信息
     *
     * @param crossOrigin CrossOrigin注解(可以为null)
     * @param corsConfig CorsConfig
     */
    private fun updateCorsConfig(corsConfig: CorsConfiguration, @Nullable crossOrigin: CrossOrigin?) {
        crossOrigin ?: return

        // set AllowedMethods
        crossOrigin.methods.map(RequestMethod::name).forEach(corsConfig::addAllowedMethod)

        // set AllowedHeaders
        crossOrigin.allowedHeaders.map(this::resolveCorsAnnotationValue).forEach(corsConfig::addAllowedHeader)

        // set ExposedHeaders
        crossOrigin.exposedHeaders.map(this::resolveCorsAnnotationValue).forEach(corsConfig::addExposeHeader)

        // set AllowedOrigins
        crossOrigin.origins.map(this::resolveCorsAnnotationValue).forEach(corsConfig::addAllowedOrigin)

        // set AllowOriginPatterns
        crossOrigin.originPatterns.map(this::resolveCorsAnnotationValue).forEach(corsConfig::addAllowedOriginPattern)

        // set AllowCredentials
        corsConfig.setAllowCredentials(crossOrigin.allowCredentials.toBoolean())

        // set MaxAge
        if (crossOrigin.maxAge >= 0) {
            corsConfig.setMaxAge(crossOrigin.maxAge)
        }
    }

    /**
     * 如果必要的话, 使用嵌入式的值解析器, 去解析Cors注解的值
     *
     * @param value value
     * @return 解析结果(无法解析的话, return "")
     */
    private fun resolveCorsAnnotationValue(value: String): String {
        return if (this.embeddedValueResolver != null) this.embeddedValueResolver?.resolveStringValue(value) ?: ""
        else value
    }

    /**
     * 联合两个[RequestMappingInfo]当中的相关信息, 合并成为一个最终的[RequestMappingInfo]
     *
     * @param classMapping 类上的[RequestMappingInfo]信息
     * @param methodMapping 方法上的[RequestMappingInfo]信息
     * @return 联合类上的[RequestMapping]和方法上的[RequestMapping]之后的新的[RequestMappingInfo]
     */
    protected open fun combine(
        classMapping: RequestMappingInfo,
        methodMapping: RequestMappingInfo
    ): RequestMappingInfo {
        val combinedMethods = classMapping.methodsCondition.combine(methodMapping.methodsCondition)
        val combinedPath = classMapping.pathPatternsCondition.combine(methodMapping.pathPatternsCondition)
        val combinedParam = classMapping.paramsCondition.combine(methodMapping.paramsCondition)
        val combinedHeader = classMapping.headersCondition.combine(methodMapping.headersCondition)
        val combinedProduces = classMapping.producesCondition.combine(methodMapping.producesCondition)
        val combinedConsumes = classMapping.consumesCondition.combine(methodMapping.consumesCondition)
        return RequestMappingInfo(
            combinedMethods, combinedPath, combinedParam, combinedHeader, combinedProduces, combinedConsumes
        )
    }

    /**
     * 从给定的方法/类上去解析`@RequestMapping`注解, 并封装成为RequestMappingInfo对象
     *
     * @param element 要去进行寻找@RequestMapping注解的目标方法/目标类
     * @return 如果找到了@RequestMapping注解的话, 那么返回包装好的[RequestMappingInfo]对象; 找不到return null
     */
    @Nullable
    protected open fun getRequestMappingInfo(element: AnnotatedElement): RequestMappingInfo? {
        val requestMapping =
            AnnotatedElementUtils.getMergedAnnotation(element, RequestMapping::class.java) ?: return null
        return RequestMappingInfo.Builder()
            .methods(*requestMapping.method)
            .paths(*resolveEmbeddedValuesInPatterns(requestMapping.path))
            .params(*requestMapping.params)
            .headers(*requestMapping.headers)
            .consumes(*requestMapping.consumes)
            .produces(*requestMapping.produces)
            .build()
    }

    /**
     * 如果必要的话, 需要去为配置的路径去进行嵌入式表达式的解析工作
     *
     * @param paths 原始的路径信息(可能含有占位符, 需要去进行占位符的解析)
     * @return 解析完成占位符之后的路径信息
     */
    protected open fun resolveEmbeddedValuesInPatterns(paths: Array<String>): Array<String> {
        val embeddedValueResolver = this.embeddedValueResolver ?: return paths
        return paths.map { embeddedValueResolver.resolveStringValue(it)!! }.toTypedArray()
    }
}