package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.ConverterRegistry

/**
 * 这是一个默认的ConversionService，它是一个支持泛型的ConversionService，并且添加了很多默认的Converter，去提供基础的类型转换功能
 *
 * @see GenericConversionService
 * @see ConfigurableConversionService
 */
open class DefaultConversionService : GenericConversionService() {
    init {
        // 添加默认的Converters
        addDefaultConverters(this)
    }

    companion object {

        // 共享的实例
        private var sharedInstance: DefaultConversionService? = null

        /**
         * 获取共享的ConversionService，使用DCL完成获取
         */
        @JvmStatic
        fun getSharedInstance(): DefaultConversionService {
            var sharedInstance = DefaultConversionService.sharedInstance
            if (sharedInstance == null) {
                synchronized(DefaultConversionService::class.java) {
                    sharedInstance = DefaultConversionService.sharedInstance
                    if (sharedInstance == null) {
                        sharedInstance = DefaultConversionService()
                    }
                }
            }
            return sharedInstance!!
        }

        @JvmStatic
        fun addDefaultConverters(registry: ConverterRegistry) {
            registry.addConverter(StringToNumberConverter())
        }
    }
}