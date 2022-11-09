package com.wanna.framework.beans

import com.wanna.framework.core.convert.support.DefaultConversionService

/**
 * BeanWrapper的具体实现，提供了属性的访问器，并组合了BeanFactory的TypeConverter，去完成Bean属性的类型的转换工作；
 *
 * 它提供了SpringBean的BeanDefinition当中的PropertyValues(pvs)当中维护的所有的属性值的设置工作，在Spring的BeanDefinition当中，
 * 通过BeanDefinition去添加PropertyValue的方式，可以去实现属性的自动注入工作；
 *
 * 当然，别的情况下，也支持去进行设置，只要你能提供目标对象以及相应的PropertyValues列表即可
 */
open class BeanWrapperImpl(beanInstance: Any? = null) : BeanWrapper, AbstractNestablePropertyAccessor() {
    init {
        if (beanInstance != null) {
            super.setWrappedInstance(beanInstance)
        }
        super.setConversionService(DefaultConversionService.getSharedInstance())
    }
}