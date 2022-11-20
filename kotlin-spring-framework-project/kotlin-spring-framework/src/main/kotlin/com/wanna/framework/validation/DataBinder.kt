package com.wanna.framework.validation

import com.wanna.framework.beans.*
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.beans.PropertyEditor
import java.util.*

/**
 * 数据绑定器，组合了TypeConverter，支持去进行类型的转换工作，并支持参数的检验工作
 *
 * @param target 要去进行绑定的目标对象
 * @param objectName objectName
 *
 * @see TypeConverter
 * @see Validator
 */
open class DataBinder(private val target: Any?, private val objectName: String = DEFAULT_OBJECT_NAME) :
    PropertyEditorRegistry, TypeConverter {

    companion object {
        private const val DEFAULT_OBJECT_NAME = "target"
    }

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(DataBinder::class.java)

    /**
     * TypeConverter
     */
    private val typeConverter = SimpleTypeConverter()

    /**
     * ConversionService
     */
    @Nullable
    private var conversionService: ConversionService? = DefaultConversionService.getSharedInstance()

    /**
     * BindingResult
     */
    @Nullable
    private var bindingResult: AbstractPropertyBindingResult? = null

    // Validator列表，用来提供参数的检验
    private val validators = ArrayList<Validator>()

    init {
        typeConverter.setConversionService(DefaultConversionService())
    }

    /**
     * 获取BindingResult
     *
     * @return BindingResult
     */
    open fun getBindingResult(): BindingResult = getInternalBindingResult()

    /**
     * 获取BindingResult，如果没有的话，需要提前完成创建
     *
     * @return 创建好的BindingResult
     */
    protected open fun getInternalBindingResult(): AbstractPropertyBindingResult {
        if (this.bindingResult == null) {
            this.bindingResult = createBeanPropertyBindingResult()
        }
        return bindingResult ?: throw AssertionError("不应该到达这里")
    }

    /**
     * create BeanPropertyBindingResult
     */
    open fun createBeanPropertyBindingResult(): AbstractPropertyBindingResult {
        val result = BeanPropertyBindingResult(getTarget(), getObjectName())
        Optional.ofNullable(conversionService).ifPresent(result::initConversion)
        return result
    }

    /**
     * 使用内部的Validator，对内部的目标对象去进行检验
     */
    open fun validate() {
        val target = getTarget() ?: throw IllegalStateException("要去进行绑定的目标对象不能为空")
        val bindingResult = getBindingResult()
        validators.forEach { it.validate(target, bindingResult) }
    }

    /**
     * 使用内部的Spring实现的Validator，对内部的目标对象去进行检验，这个方法当中新增对于Hints的支持
     *
     * @param validationHints JSR303的Validation的Hints
     */
    open fun validate(vararg validationHints: Any) {
        val target = getTarget() ?: throw IllegalStateException("要去进行绑定的目标对象不能为空")
        val bindingResult = getBindingResult()
        validators.forEach {
            if (validationHints.isNotEmpty() && it is SmartValidator) {
                it.validate(target, bindingResult, *validationHints)
            } else {
                it.validate(target, bindingResult)
            }
        }
    }

    /**
     * 设置Validator，把之前的Validator全清空掉
     *
     * @param validator 要使用的Validator
     */
    open fun setValidator(validator: Validator) {
        assertValidator(validator)
        this.validators.clear()
        this.validators += validator
    }

    /**
     * 添加Validator
     *
     * @param validators 想要添加的Validator列表
     */
    open fun addValidators(vararg validators: Validator) {
        assertValidators(*validators)
        validators.forEach(this.validators::add)
    }

    /**
     * 替换之前的所有的Validator
     *
     * @param validators 要使用的最终Validator列表
     */
    open fun replaceValidators(vararg validators: Validator) {
        assertValidators(*validators)
        this.validators.clear()  //clear
        validators.forEach(this.validators::add)
    }

    /**
     * 断言给定的所有的Validator都能处理当前的类型的对象参数检验
     *
     * @param validators Validators
     */
    private fun assertValidators(vararg validators: Validator) {
        validators.forEach(this::assertValidator)
    }

    /**
     * 断言给定的Validator能处理当前的target对象的检验
     *
     * @param validator 待断言的Validator
     */
    private fun assertValidator(validator: Validator) {
        val target = getTarget() ?: throw IllegalStateException("要去进行绑定的目标对象不能为空")
        if (validator.supports(target::class.java)) {
            throw IllegalStateException("添加的Validator不支持去处理这样的目标类型[${target::class.java}]")
        }
    }

    /**
     * 获取SimpleTypeConverter
     *
     * @return SimpleTypeConverter
     */
    protected open fun getSimpleTypeConverter(): SimpleTypeConverter = this.typeConverter

    /**
     * 获取PropertyAccessor
     *
     * @return ConfigurablePropertyAccessor
     */
    protected open fun getPropertyAccessor(): ConfigurablePropertyAccessor =
        getInternalBindingResult().getPropertyAccessor()

    open fun getTypeConverter(): TypeConverterSupport = this.typeConverter

    override fun registerCustomEditor(requiredType: Class<*>, propertyEditor: PropertyEditor) {
        getTypeConverter().registerCustomEditor(requiredType, propertyEditor)
    }

    override fun findCustomEditor(requiredType: Class<*>): PropertyEditor? {
        return getTypeConverter().findCustomEditor(requiredType)
    }

    override fun <T : Any> convertIfNecessary(value: Any?, requiredType: Class<T>?): T? {
        return getTypeConverter().convertIfNecessary(value, requiredType)
    }

    /**
     * set ConversionService，不仅需要去设置TypeConverter的Conversion，还需要去设置BindingResult的ConversionService
     *
     * @param conversionService ConversionService
     */
    open fun setConversionService(conversionService: ConversionService) {
        this.conversionService = conversionService  // set
        getTypeConverter().setConversionService(conversionService)
        this.bindingResult?.initConversion(conversionService)
    }

    open fun getConversionService(): ConversionService? = this.conversionService

    /**
     * 获取包装的需要去进行绑定的目标对象
     *
     * @return 待绑定的目标对象
     */
    open fun getTarget(): Any? = this.target

    /**
     * 获取目标对象的name
     *
     * @return objectName
     */
    open fun getObjectName(): String = this.objectName
}