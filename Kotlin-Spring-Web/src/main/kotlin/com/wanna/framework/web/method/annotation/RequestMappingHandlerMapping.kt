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
        return AnnotatedElementUtils.isAnnotated(beanType, Controller::class.java) ||
                AnnotatedElementUtils.isAnnotated(beanType, RequestMapping::class.java)
    }

    /**
     * 给定handlerMethod和handlerType，返回Mapping(RequestMappingInfo)
     *
     * @param method method
     * @param handlerType handlerType
     * @return 如果方法上找到了@RequestMapping注解，return封装好的RequestMappingInfo；不然return null
     */
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        return getRequestMappingInfo(method)
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

        return RequestMappingInfo.Builder().paths(*requestMapping.path).params(*requestMapping.params).build()
    }
}