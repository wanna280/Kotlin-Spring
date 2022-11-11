package com.wanna.boot.actuate.web.mappings

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.web.HandlerMapping

/**
 * 提供对于一个[ApplicationContext]当中的全部[HandlerMapping]的描述信息的Provider
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 */
interface MappingDescriptionProvider {

    /**
     * 获取MappingName
     *
     * @return mappingName
     */
    fun getMappingName(): String

    /**
     * 对于一个ApplicationContext当中的所有Mappings去进行描述
     *
     * @param applicationContext ApplicationContext
     * @return 对于一个[ApplicationContext]当中的全部的[HandlerMapping]的描述结果
     */
    fun describeMappings(applicationContext: ApplicationContext): Any?
}