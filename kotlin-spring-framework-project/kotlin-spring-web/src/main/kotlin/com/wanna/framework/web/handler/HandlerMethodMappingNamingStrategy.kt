package com.wanna.framework.web.handler

import com.wanna.framework.web.method.HandlerMethod

/**
 * 这是一个HandlerMethod的Mapping的命名策略，可以根据handlerMethod和mapping去对放入注册中心当中的name去进行生成
 *
 * @param T mappingType，子类自己指定
 */
interface HandlerMethodMappingNamingStrategy<T> {
    /**
     * @param handlerMethod handlerMethod
     * @param mapping mapping
     * @return 根据命名策略生成的name
     */
    fun getName(handlerMethod: HandlerMethod, mapping: T) : String
}