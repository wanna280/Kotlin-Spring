package com.wanna.framework.context.format.support

import com.wanna.framework.context.format.FormatterRegistry
import com.wanna.framework.core.convert.support.GenericConversionService

/**
 * 它是一个提供了Formatter的ConversionService
 *
 * @see FormatterRegistry
 * @see GenericConversionService
 */
open class FormattingConversionService : GenericConversionService(), FormatterRegistry {

}