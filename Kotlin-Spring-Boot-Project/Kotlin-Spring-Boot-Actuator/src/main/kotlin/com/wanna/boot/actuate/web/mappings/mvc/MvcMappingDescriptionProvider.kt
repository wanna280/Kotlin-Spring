package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.boot.actuate.web.mappings.MappingDescriptionProvider
import com.wanna.framework.context.ApplicationContext

/**
 * 提供对于Mvc当中的Mapping去进行描述的Provider
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 */
open class MvcMappingDescriptionProvider : MappingDescriptionProvider {

    override fun getMappingName(): String = "mvc"

    override fun describeMappings(applicationContext: ApplicationContext): Any? {
        return null
    }
}