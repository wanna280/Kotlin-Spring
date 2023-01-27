package com.wanna.boot.autoconfigure.jackson

import com.wanna.framework.web.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * 对[Jackson2ObjectMapperBuilder]去进行自定义的自定义化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 */
fun interface Jackson2ObjectMapperBuilderCustomizer {

    /**
     * 执行对于[Jackson2ObjectMapperBuilder]的自定义
     *
     * @param builder builder
     */
    fun customize(builder: Jackson2ObjectMapperBuilder)
}