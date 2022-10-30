package com.wanna.boot.actuate.web.mappings

import com.wanna.framework.context.ApplicationContext

/**
 * 提供Mapping的描述信息的Provider
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
     * 对于Mappings去进行描述
     *
     * @param applicationContext ApplicationContext
     * @return 描述结果
     */
    fun describeMappings(applicationContext: ApplicationContext): Any?
}