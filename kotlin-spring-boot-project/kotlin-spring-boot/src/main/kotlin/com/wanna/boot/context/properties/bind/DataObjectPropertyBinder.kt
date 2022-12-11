package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable

/**
 * 被[DataObjectBinder]去用于提供属性的绑定功能的Binder, 因为对于[JavaBeanBinder]内部绑定时,
 * 需要递归调用[Binder]当中的相关方法, 因此就需要提供这么一个Callback方法, 这个接口存在的意义就是提供Callback
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
fun interface DataObjectPropertyBinder {

    /**
     * 对于给定的属性去进行绑定
     *
     * @param propertyName 待绑定的Java对象的属性的属性名
     * @param target 要去进行绑定的字段的相关信息的Bindable
     * @return propertyName对应的字段绑定结果
     */
    @Nullable
    fun bindProperty(propertyName: String, target: Bindable<Any>): Any?
}