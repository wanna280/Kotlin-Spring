package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.ConverterRegistry

/**
 * 这是一个默认的ConversionService的实现，它是一个支持泛型的ConversionService，
 * 并且添加了很多默认的Converter，去提供基础的类型转换功能
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
         *
         * @return 共享的DefaultConversionService
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
            registry.addConverter(ArrayToStringConverter())
            registry.addConverter(CollectionToStringConverter())

            if (registry is ConversionService) {
                // Collection To Array/Collection
                registry.addConverter(CollectionToArrayConverter(registry))
                registry.addConverter(CollectionToCollectionConverter(registry))

                // String to Array/Collection
                registry.addConverter(StringToCollectionConverter(registry))
                registry.addConverter(StringToArrayConverter(registry))
            }
            registry.addConverter(StringToStringConverter())
        }
    }
}