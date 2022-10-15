package com.wanna.framework.web.method

import com.wanna.framework.web.handler.HandlerMethodMappingNamingStrategy

/**
 * 这是一个对RequestMappingInfo的HandlerMethod去进行命名的策略
 *
 * @see RequestMappingInfo
 */
open class RequestMappingInfoHandlerMethodMappingNamingStrategy :
    HandlerMethodMappingNamingStrategy<RequestMappingInfo> {

    companion object {
        private const val SEPARATOR = "#"
    }

    /**
     * 生成name，设置为handlerBeanType的当中的大写字母#handlerMethodName，
     * 例如HandlerBeanType为UserController，handlerMethodName=getUser，那么return UC#getUser
     *
     * @param handlerMethod HandlerMethod
     * @param mapping RequestMappingInfo
     * @return 根据策略去生成的name
     */
    override fun getName(handlerMethod: HandlerMethod, mapping: RequestMappingInfo): String {
        val builder = StringBuilder()
        // 拼接HandlerMethod当中的大写字母
        val name = handlerMethod.beanType!!.name
        name.filter { it.isUpperCase() }.forEach(builder::append)
        // append方法名
        builder.append(SEPARATOR).append(handlerMethod.method!!.name)
        return builder.toString()
    }
}