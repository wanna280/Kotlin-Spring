package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.boot.actuate.web.mappings.MappingDescriptionProvider
import com.wanna.framework.context.ApplicationContext

/**
 * 对外提供所有的RequestMapping的Endpoint
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 *
 * @param applicationContext ApplicationContext
 * @param descriptionProviders 提供Mapping描述信息的获取的Provider
 */
@Endpoint("mappings")
open class MappingsEndpoint(
    private val applicationContext: ApplicationContext,
    private val descriptionProviders: List<MappingDescriptionProvider>
) {

    /**
     * 对外提供读操作的接口, 用于提供ApplicationContext当中的Mapping的描述信息
     *
     * @return ApplicationMappings
     */
    @ReadOperation
    open fun mappings(): ApplicationMappings {

        // contextMappings(key-contextId, value-ContextMappings)
        val contextMappings = LinkedHashMap<String, ContextMappings>()

        // 遍历所有的parentApplicationContext去进行merge, 得到所有的ApplicationContext当中的Mappings信息
        var target: ApplicationContext? = this.applicationContext
        while (target != null) {
            contextMappings[target.getId()] = mappingsForContext(target)
            target = target.getParent()
        }

        // 将所有的ContextMappings去merge到ApplicationMappings当中
        return ApplicationMappings(contextMappings)
    }

    /**
     * 为一个[ApplicationContext]从所有的[MappingDescriptionProvider]当中去获取到所有的Mapping信息去进行merge
     *
     * @param applicationContext ApplicationContext
     * @return 从该ApplicationContext当中得到的所有的描述信息的merge结果
     */
    private fun mappingsForContext(applicationContext: ApplicationContext): ContextMappings {
        val mappings = LinkedHashMap<String, Any?>()
        descriptionProviders.forEach { mappings[it.getMappingName()] = it.describeMappings(applicationContext) }
        return ContextMappings(
            if (applicationContext.getParent() != null) applicationContext.getParent()!!.getId() else null,
            mappings
        )
    }

    /**
     * 描述了多个ApplicationContext当中的Mappings信息(一个ApplicationContext以及它的所有的parentApplicationContext)
     *
     * @param mappings mappings(Key-contextId, Value-一个ApplicationContext的描述信息)
     */
    data class ApplicationMappings(val mappings: Map<String, ContextMappings>)

    /**
     * 对于单个ApplicationContext当中的多个[MappingDescriptionProvider]得到描述信息的merge结果
     *
     * @param parentId parentContextId(如果存在的话, 不存在的话, 为null)
     * @param mappings 一个元素就代表了一个[MappingDescriptionProvider]的描述信息(Key-mappingName, Value-MappingDescriptionValue)
     */
    data class ContextMappings(val parentId: String?, val mappings: Map<String, Any?>)
}