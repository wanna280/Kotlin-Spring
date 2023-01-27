package com.wanna.framework.web.http.converter.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.framework.context.ApplicationContext

/**
 * Jackson2的ObjectMapper的Builder
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
open class Jackson2ObjectMapperBuilder {

    /**
     * 是否要创建xmlMapper?
     */
    private var createXmlMapper = false

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    open fun createXmlMapper(createXmlMapper: Boolean): Jackson2ObjectMapperBuilder {
        this.createXmlMapper = createXmlMapper
        return this
    }

    open fun applicationContext(applicationContext: ApplicationContext): Jackson2ObjectMapperBuilder {
        this.applicationContext = applicationContext
        return this
    }

    /**
     * 执行构建[ObjectMapper]
     *
     * @return ObjectMapper
     */
    open fun build(): ObjectMapper {
        // TODO
        return ObjectMapper()
    }

}