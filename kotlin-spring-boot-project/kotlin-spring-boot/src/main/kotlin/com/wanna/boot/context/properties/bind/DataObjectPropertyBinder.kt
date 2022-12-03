package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable

/**
 * 被[DataObjectBinder]去用于提供属性的绑定功能的Binder
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