package com.wanna.framework.beans

import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.lang.Nullable

/**
 * BeanWrapper的具体实现，提供了属性的访问器，并组合了BeanFactory的TypeConverter，去完成Bean属性的类型的转换工作；
 *
 * 它提供了SpringBean的BeanDefinition当中的PropertyValues(pvs)当中维护的所有的属性值的设置工作，在Spring的BeanDefinition当中，
 * 通过BeanDefinition去添加PropertyValue的方式，可以去实现属性的自动注入工作；
 *
 * 当然，别的情况下，也支持去进行设置，只要你能提供目标对象以及相应的PropertyValues列表即可
 *
 * @see BeanWrapper
 * @see AbstractNestablePropertyAccessor
 */
open class BeanWrapperImpl() : BeanWrapper, AbstractNestablePropertyAccessor() {

    init {
        this.setConversionService(DefaultConversionService.getSharedInstance())
    }

    constructor(@Nullable wrappedObject: Any?) : this() {
        this.wrappedObject = wrappedObject
    }

    constructor(obj: Any, @Nullable nestedPath: String?, @Nullable rootObject: Any?) : this() {
        this.wrappedObject = obj
        this.nestedPath = nestedPath ?: ""
        this.rootObject = rootObject
    }

    constructor(obj: Any, @Nullable nestedPath: String?, parent: AbstractNestablePropertyAccessor) : this() {
        this.wrappedObject = obj
        this.nestedPath = nestedPath ?: ""
        this.setConversionService(parent.getConversionService())
        this.autoGrowNestedPaths = parent.autoGrowNestedPaths
        this.rootObject = parent.getWrappedInstance()
    }

    /**
     * 创建一个嵌套的[AbstractNestablePropertyAccessor]
     *
     * @param instance instance
     * @param nestedPath nestedPath
     * @return 创建出来的嵌套的[AbstractNestablePropertyAccessor]实例
     */
    override fun newNestedPropertyAccessor(instance: Any, nestedPath: String): AbstractNestablePropertyAccessor {
        return BeanWrapperImpl(instance, nestedPath, this)
    }
}