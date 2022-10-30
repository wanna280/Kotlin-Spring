package com.wanna.boot.actuate.autoconfigure.web.mappings.mvc

import com.wanna.boot.actuate.web.mappings.MappingDescriptionProvider
import com.wanna.boot.actuate.web.mappings.mvc.MappingsEndpoint
import com.wanna.boot.actuate.web.mappings.mvc.MvcMappingDescriptionProvider
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.MVC
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.web.DispatcherHandler

/**
 * 提供对于[MappingsEndpoint]的自动装配，对外提供所有的RequestMapping映射的Endpoint
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 */
@Configuration(proxyBeanMethods = false)
open class MappingsEndpointAutoConfiguration {

    /**
     * 给SpringBeanFactory当中去装配一个[MappingsEndpoint]
     *
     * @param applicationContext ApplicationContext, SpringBeanFactory自动装配
     * @param providers 提供对于Mapping的描述信息的Provider, SpringBeanFactory自动装配
     * @return MappingsEndpoint
     */
    @Bean
    @ConditionalOnMissingBean
    open fun mappingsEndpoint(
        applicationContext: ApplicationContext,
        providers: List<MappingDescriptionProvider>
    ): MappingsEndpoint {
        return MappingsEndpoint(applicationContext, providers)
    }

    @ConditionalOnWebApplication(MVC)
    @ConditionalOnClass(name = ["com.wanna.framework.web.DispatcherHandler"])
    @ConditionalOnBean(value = [DispatcherHandler::class])
    @Configuration(proxyBeanMethods = false)
    open class MvcWebConfiguration {

        @Bean
        open fun mvcMappingDescriptionProvider(): MappingDescriptionProvider {
            return MvcMappingDescriptionProvider()
        }
    }
}