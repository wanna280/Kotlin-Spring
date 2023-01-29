package com.wanna.framework.validation

/**
 * 抽象的BindingResult的封装
 *
 * @see BindingResult
 * @see AbstractErrors
 */
abstract class AbstractBindingResult(private val objectName: String) : BindingResult, AbstractErrors() {

    override fun getObjectName() = this.objectName

}