package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.ConverterRegistry
import java.nio.charset.Charset
import java.util.*

/**
 * 这是一个默认的ConversionService的实现, 它是一个支持泛型的ConversionService,
 * 并且添加了很多默认的Converter, 去提供基础的类型转换功能
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
        /**
         * 共享的[DefaultConversionService]实例
         */
        @Volatile
        @JvmStatic
        private var sharedInstance: DefaultConversionService? = null

        /**
         * 获取共享的ConversionService, 使用DCL完成获取
         *
         * @return 共享的DefaultConversionService
         */
        @JvmStatic
        fun getSharedInstance(): DefaultConversionService {
            var sharedInstance = this.sharedInstance
            if (sharedInstance == null) {
                synchronized(DefaultConversionService::class.java) {
                    sharedInstance = this.sharedInstance
                    if (sharedInstance == null) {
                        sharedInstance = DefaultConversionService()
                    }
                }
            }
            return sharedInstance!!
        }

        /**
         * 添加一些默认的Converter到[ConverterRegistry]当中
         *
         * @param registry ConverterRegistry
         */
        @JvmStatic
        fun addDefaultConverters(registry: ConverterRegistry) {
            // 添加一些简单类型的转换的Converter
            addScalarConverters(registry)

            // 添加Array->String的Converter
            registry.addConverter(ArrayToStringConverter())

            // 添加Collection->String的Converter
            registry.addConverter(CollectionToStringConverter())
            if (registry is ConversionService) {
                // 添加Collection->Array的Converter
                registry.addConverter(CollectionToArrayConverter(registry))
                // 添加Collection->Collection的Converter
                registry.addConverter(CollectionToCollectionConverter(registry))
                // 添加Array->Collection的Converter
                registry.addConverter(ArrayToCollectionConverter(registry))

                // 添加String->Collection的Converter
                registry.addConverter(StringToCollectionConverter(registry))

                // 添加String->Array的Converter
                registry.addConverter(StringToArrayConverter(registry))

                // 添加Map->Map的Converter
                registry.addConverter(MapToMapConverter(registry))
            }
        }

        @JvmStatic
        private fun addScalarConverters(registry: ConverterRegistry) {
            // 添加String->String的Converter
            registry.addConverter(StringToStringConverter())

            // 添加String->bool的Converter
            registry.addConverter(String::class.java, Boolean::class.java, StringToBooleanConverter())
            // 添加String->Boolean的Converter
            registry.addConverter(String::class.java, Boolean::class.javaObjectType, StringToBooleanConverter())
            // 添加String->Character的Converter
            registry.addConverter(StringToCharacterConverter())

            // 添加String->Number的Converter
            registry.addConverter(StringToNumberConverter())
            // 添加Number->String的Converter
            registry.addConverter(Number::class.java, String::class.java, ObjectToStringConverter())
            // 添加Number->NumberObject&NumberObject->Number的Converter
            registry.addConverter(NumberNumberObjectConverter())

            // 添加从Boolean->String的Converter
            registry.addConverter(Boolean::class.java, String::class.java, ObjectToStringConverter())

            // 添加从String->Charset的Converter
            registry.addConverter(StringToCharsetConverter())
            // 添加从Charset->String的Converter
            registry.addConverter(Charset::class.java, String::class.java, ObjectToStringConverter())

            // 添加Locale->String的Converter
            registry.addConverter(Locale::class.java, String::class.java, ObjectToStringConverter())

            // 添加String->UUID的Converter
            registry.addConverter(StringToUUIDConverter())
            // 添加UUID->String的Converter
            registry.addConverter(UUID::class.java, String::class.java, ObjectToStringConverter())

            // 添加String->Enum的Converter
            registry.addConverter(StringToEnumConverter())
            // 添加Enum->String的Converter
            registry.addConverter(Enum::class.java, String::class.java, ObjectToStringConverter())
        }
    }
}