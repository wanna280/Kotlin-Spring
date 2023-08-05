package com.wanna.framework.validation

import com.wanna.framework.beans.BeanWrapper
import com.wanna.framework.beans.BeanWrapperImpl
import com.wanna.framework.beans.ConfigurablePropertyAccessor
import com.wanna.framework.beans.PropertyEditorRegistry

/**
 * 基于BeanProperty的BindingResult
 *
 * // TODO
 *
 * @see BindingResult
 * @see Errors
 */
open class BeanPropertyBindingResult(private val target: Any?, objectName: String) :
    AbstractPropertyBindingResult(objectName) {

    /**
     * Errors
     */
    private val errors = ArrayList<ObjectError>()

    override fun getTarget() = this.target

    override fun getPropertyAccessor(): ConfigurablePropertyAccessor {
        return createBeanWrapper()
    }

    override fun getModel(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    override fun getPropertyEditorRegistry(): PropertyEditorRegistry {
        TODO("Not yet implemented")
    }

    override fun reject(errorCode: String) {

    }

    override fun reject(errorCode: String, defaultMessage: String) {

    }

    override fun hasErrors(): Boolean {
        return this.errors.isNotEmpty()
    }

    override fun getAllErrors(): List<ObjectError> = this.errors

    override fun getFieldError(name: String): FieldError? {
        return null // TODO
    }

    override fun addError(error: ObjectError) {
        this.errors.add(error)
    }

    protected open fun createBeanWrapper(): BeanWrapper {
        return BeanWrapperImpl()
    }
}