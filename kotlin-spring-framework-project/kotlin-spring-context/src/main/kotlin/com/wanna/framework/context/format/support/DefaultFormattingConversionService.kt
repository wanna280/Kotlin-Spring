package com.wanna.framework.context.format.support

import com.wanna.framework.core.convert.support.DefaultConversionService

open class DefaultFormattingConversionService : FormattingConversionService() {

    /**
     * 添加默认的ConversionService
     */
    init {
       DefaultConversionService.addDefaultConverters(this)
   }
}