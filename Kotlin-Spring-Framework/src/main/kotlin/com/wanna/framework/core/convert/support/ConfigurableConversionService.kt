package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.ConverterRegistry

/**
 * 这是一个可以被配置的ConversionService，它本身已经集成了Converter，可以去完成Converter的管理工作
 *
 * @see ConversionService
 * @see ConverterRegistry
 */
interface ConfigurableConversionService : ConversionService, ConverterRegistry {

}