package com.wanna.framework.validation

import com.wanna.framework.beans.ConfigurablePropertyAccessor
import com.wanna.framework.core.convert.ConversionService

/**
 * 基于属性的BindingResult的实现
 *
 * @see BeanPropertyBindingResult
 * @see
 */
abstract class AbstractPropertyBindingResult(objectName: String) : AbstractBindingResult(objectName) {

    // ConversionService
    private var conversionService: ConversionService? = null

    /**
     * 初始化ConversionService
     *
     * @param conversion ConversionService
     */
    open fun initConversion(conversion: ConversionService) {
        this.conversionService = conversion
        getPropertyAccessor().setConversionService(conversion)
    }

    /**
     * 获取PropertyAccessor, 具体的获取逻辑交给子类去进行实现
     *
     * @return ConfigurablePropertyAccessor
     */
    abstract fun getPropertyAccessor(): ConfigurablePropertyAccessor
}