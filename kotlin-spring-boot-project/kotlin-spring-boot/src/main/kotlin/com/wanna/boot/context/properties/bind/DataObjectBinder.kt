package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.lang.Nullable

/**
 * 提供对于一个DataObject(数据对象)的属性绑定功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @see ValueObjectBinder
 * @see JavaBeanBinder
 */
interface DataObjectBinder {

    /**
     * 对于一个给定的属性名的对象属性, 去进行绑定
     *
     * @param name 待进行绑定的属性名的前缀Key
     * @param target 要去进行绑定的目标对象的Bindable
     * @param context Binder去进行绑定属性时用到的上下文信息
     * @param propertyBinder 对单个属性去提供绑定的PropertyBinder回调函数
     * @return 绑定完成的实例对象(or null, null表示绑定失败)
     */
    @Nullable
    fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        context: Binder.Context,
        propertyBinder: DataObjectPropertyBinder
    ): T?

    /**
     * 实例化目标DataObject对象
     *
     * @param target 要去进行绑定的目标对象的Bindable
     * @param context Binder去进行绑定属性用到的上下文信息
     * @return 创建出来的目标实例对象(or null)
     */
    @Nullable
    fun <T : Any> create(target: Bindable<T>, context: Binder.Context): T?
}