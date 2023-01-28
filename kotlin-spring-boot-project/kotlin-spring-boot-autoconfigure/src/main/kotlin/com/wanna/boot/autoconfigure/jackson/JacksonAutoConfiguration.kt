package com.wanna.boot.autoconfigure.jackson

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.*
import com.wanna.framework.core.Ordered
import com.wanna.framework.web.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Jackson的自动配置类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
@ConditionalOnClass([ObjectMapper::class])
@Configuration(proxyBeanMethods = false)
open class JacksonAutoConfiguration {

    /**
     * Jackson的ObjectMapper的配置类
     */
    @ConditionalOnClass(name = ["com.wanna.framework.web.http.converter.json.Jackson2ObjectMapperBuilder"])
    @Configuration(proxyBeanMethods = false)
    open class JacksonObjectMapperConfiguration {
        @Bean
        @Primary
        @ConditionalOnMissingBean
        open fun jacksonObjectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
            return builder.createXmlMapper(false).build()
        }
    }

    /**
     * Jackson的ObjectMapperBuilder的配置类
     */
    @ConditionalOnClass(name = ["com.wanna.framework.web.http.converter.json.Jackson2ObjectMapperBuilder"])
    @Configuration(proxyBeanMethods = false)
    open class JacksonObjectMapperBuilderConfiguration {
        @Bean
        @Scope(BeanDefinition.SCOPE_PROTOTYPE)
        @ConditionalOnMissingBean
        open fun jacksonObjectMapperBuilder(
            applicationContext: ApplicationContext,
            @Autowired(required = false) customizers: List<Jackson2ObjectMapperBuilderCustomizer>
        ): Jackson2ObjectMapperBuilder {
            val builder = Jackson2ObjectMapperBuilder()
            builder.applicationContext(applicationContext)
            customize(customizers, builder)
            return builder
        }

        private fun customize(
            customizers: List<Jackson2ObjectMapperBuilderCustomizer>,
            builder: Jackson2ObjectMapperBuilder
        ) {
            customizers.forEach { it.customize(builder) }
        }
    }

    /**
     * Jackson的ObjectMapperBuilder的自定义化器的配置类
     */
    @EnableConfigurationProperties([JacksonProperties::class])
    @Configuration(proxyBeanMethods = false)
    open class Jackson2ObjectMapperBuilderCustomizerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        open fun standardJackson2ObjectMapperBuilderCustomizer(
            jacksonProperties: JacksonProperties,
            @Autowired(required = false) modules: List<Module>
        ): StandardJackson2ObjectMapperBuilderCustomizer {
            return StandardJackson2ObjectMapperBuilderCustomizer(jacksonProperties, modules)
        }


        /**
         * 标准的[Jackson2ObjectMapperBuilderCustomizer]实现
         *
         * @param jacksonProperties Jackson的配置信息
         * @param modules Modules
         */
        class StandardJackson2ObjectMapperBuilderCustomizer(
            private val jacksonProperties: JacksonProperties,
            private val modules: List<Module>
        ) : Jackson2ObjectMapperBuilderCustomizer, Ordered {

            override fun getOrder(): Int = 0

            override fun customize(builder: Jackson2ObjectMapperBuilder) {

            }
        }
    }

}