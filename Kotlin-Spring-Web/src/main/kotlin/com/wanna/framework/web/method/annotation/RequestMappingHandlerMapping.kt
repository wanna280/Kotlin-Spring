package com.wanna.framework.web.method.annotation

import com.wanna.framework.context.stereotype.Controller
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.RequestMappingInfoHandlerMapping
import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 这是一个处理@RequestMapping注解的HandlerMapping，基本上所有的功能都在它的父类当中实现了，它需要实现相关的模板方法：
 * (1)如何判断它是一个Handler？---如果类上标注了@RequestMapping/@Controller注解，它就是一个支持当前类去进行处理的Handler
 * (2)如果解析一个HandlerMethod？---解析方法/类上的@RequestMapping注解的相关信息即可判断它是否是一个HandlerMethod
 *
 * @see RequestMapping
 * @see Controller
 */
open class RequestMappingHandlerMapping : RequestMappingInfoHandlerMapping() {

    /**
     * 怎么判断它是否是一个Handler？只需要类上加了@Controller/@RequestMapping注解，它就是一个Handler
     *
     * @param beanType beanType
     * @return 它是否是一个Handler(如果标注了@Controller/@RequestMapping注解return true)
     */
    override fun isHandler(beanType: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(beanType, Controller::class.java) || AnnotatedElementUtils.isAnnotated(
            beanType,
            RequestMapping::class.java
        )
    }

    /**
     * 给定handlerMethod和handlerType，返回Mapping(RequestMappingInfo)，这里因为方法上和类上都有可能有@RequestMapping注解，
     * 因此，我们需要去进行合并，但是由于合并的算法不会写，目前仅仅提供了路径的前缀功能，别的功能算法不会写！！！
     *
     * @param method method
     * @param handlerType handlerType
     * @return 如果方法上找到了@RequestMapping注解，return封装好的RequestMappingInfo；不然return null
     */
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        // 从方法上找到@RequestMapping注解
        val info = getRequestMappingInfo(method)
        // 如果方法上找到了@RequestMapping注解，那么尝试去类上去进行寻找
        if (info != null) {
            val typeInfo = getRequestMappingInfo(handlerType)
            // 如果类上也有@RequestMapping的话，需要联合两个RequestMappingInfo
            if (typeInfo != null) {
                return combine(info, typeInfo)
            }
        }
        return info
    }

    /**
     * 联合两个RequestMappingInfo
     *
     * @param info info1
     * @param newInfo info2
     * @return 联合之后的新的RequestMappingInfo
     */
    protected open fun combine(info: RequestMappingInfo, newInfo: RequestMappingInfo): RequestMappingInfo {
        val combinedMethods = info.methodsCondition.combine(newInfo.methodsCondition)
        val combinedPath = info.pathPatternsCondition.combine(newInfo.pathPatternsCondition)
        val combinedParam = info.paramsCondition.combine(newInfo.paramsCondition)
        val combinedHeader = info.headersCondition.combine(newInfo.headersCondition)
        return RequestMappingInfo(combinedMethods, combinedPath, combinedParam, combinedHeader)
    }

    /**
     * 从方法/类上解析@RequestMapping注解，并封装成为RequestMappingInfo对象
     *
     * @param element 目标方法/目标类
     * @return 如果找到了@RequestMapping，那么返回包装好的RequestMappingInfo对象；找不到return null
     */
    protected open fun getRequestMappingInfo(element: AnnotatedElement): RequestMappingInfo? {
        val requestMapping =
            AnnotatedElementUtils.getMergedAnnotation(element, RequestMapping::class.java) ?: return null
        return RequestMappingInfo.Builder().methods(*requestMapping.method).paths(*requestMapping.path)
            .params(*requestMapping.params).headers(*requestMapping.header).build()
    }
}