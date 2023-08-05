package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.lang.reflect.Array

/**
 * 提供对于一个Array的元素的绑定工作
 *
 * @param context Binder去进行绑定属性的上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see IndexedElementsBinder
 * @see CollectionBinder
 * @see AggregateElementBinder
 */
open class ArrayBinder<T : Any>(context: Binder.Context) : IndexedElementsBinder<T>(context) {

    /**
     * 执行对于Array这样的聚合元素的绑定
     *
     * @param name 用于绑定这个Array需要使用到的属性Key前缀
     * @param target 描述要去进行绑定的Array元素的相关信息
     * @param aggregateElementBinder 用于去对Array里的一个元素去进行提供递归绑定的功能的Callback
     * @return 对于Array的绑定结果(或者是null)
     */
    @Nullable
    @Suppress("UNCHECKED_CAST")
    override fun bindAggregate(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder
    ): Any? {
        // 创建一个提供单例的ArrayList的Supplier
        val resultSupplier = IndexedCollectionSupplier { ArrayList() }

        val aggregateType = target.type
        val resolvedAggregateType = aggregateType.resolve(Any::class.java)
        if (!resolvedAggregateType.isArray) {
            throw IllegalStateException(
                "仅仅允许Array类型的元素才能被ArrayBinder去进行绑定, 但是给定的是type=${
                    ClassUtils.getQualifiedName(resolvedAggregateType)
                }"
            )
        }
        val componentType = resolvedAggregateType.componentType
        // 执行对于IndexedElement这个元素的绑定
        bindIndexed(
            name, target, aggregateElementBinder,
            aggregateType, ResolvableType.forClass(componentType), resultSupplier
        )

        if (resultSupplier.isSupplied()) {
            // 创建出来一个Array实例对象, 将列表当中的元素拷贝过去...
            val list = resultSupplier.get() as List<Any>
            val array = Array.newInstance(componentType, list.size)
            list.indices.forEach { Array.set(array, it, list[it]) }
            return array
        }
        return null
    }
}