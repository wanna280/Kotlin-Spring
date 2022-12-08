package com.wanna.boot.context.properties

import com.wanna.boot.convert.ApplicationConversionService
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.convert.ConversionService

/**
 * 根据[ApplicationContext]去推断出来合适的[ConversionService]的推断器工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
class ConversionServiceDeducer(private val applicationContext: ApplicationContext) {

    /**
     * 根据[ApplicationContext]去推断出来合适的[ConversionService]
     *
     * @return ConversionServices
     */
    fun getConversionServices(): List<ConversionService> {
        return listOf(ApplicationConversionService.getSharedInstance())
    }
}