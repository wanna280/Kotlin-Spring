package com.wanna.framework.validation

/**
 * 绑定Bean时发生的异常
 *
 * @param bindingResult 绑定的结果信息
 */
open class BindException(val bindingResult: BindingResult) : Exception(), BindingResult {
    override fun getTarget() = bindingResult.getTarget()

    override fun getModel() = bindingResult.getModel()

    override fun getPropertyEditorRegistry() = bindingResult.getPropertyEditorRegistry()

    override fun addError(error: ObjectError) = bindingResult.addError(error)

    override fun getObjectName() = bindingResult.getObjectName()

    override fun reject(errorCode: String) = bindingResult.reject(errorCode)

    override fun reject(errorCode: String, defaultMessage: String) = bindingResult.reject(errorCode, defaultMessage)

    override fun hasErrors() = bindingResult.hasErrors()

    override fun getAllErrors() = bindingResult.getAllErrors()

    override fun getFieldError(name: String) = bindingResult.getFieldError(name)
}