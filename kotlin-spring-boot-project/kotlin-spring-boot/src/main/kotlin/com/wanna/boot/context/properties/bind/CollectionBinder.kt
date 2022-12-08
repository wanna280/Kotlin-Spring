package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable

/**
 * 提供对于一个Collection的绑定
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see IndexedElementsBinder
 * @see ArrayBinder
 * @see AggregateElementBinder
 */
open class CollectionBinder<T : Any>(context: Binder.Context) : IndexedElementsBinder<T>(context) {

    /**
     * 执行对于一个Collection聚合的元素的绑定
     *
     * @param name 对Collection去进行绑定的属性Key前缀
     * @param target 要去进行绑定的Collection的相关信息
     * @param aggregateElementBinder 对Collection当中的一个元素去提供递归绑定的Callback回调函数
     * @return 对于Collection的绑定结果(或者是null)
     */
    @Nullable
    override fun bindAggregate(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder
    ): Any? {
        // 集合类型的Class
        val collectionType = if (target.value != null) List::class.java else target.type.resolve(Any::class.java)

        // 集合类型
        val aggregateType = ResolvableType.forClassWithGenerics(
            List::class.java,
            target.type.asCollection().getGenerics()[0].resolve(Any::class.java)
        )

        // 集合的元素类型
        val elementType = target.type.asCollection().getGenerics()[0]

        // 提供单例的Collection的Supplier
        val resultSupplier = IndexedCollectionSupplier {
            CollectionFactory.createCollection(collectionType, 0)
        }

        // 使用IndexedElement的方式去进行绑定, 将真正的元素, 全部都收集到resultSupplier当中的集合里
        bindIndexed(name, target, aggregateElementBinder, aggregateType, elementType, resultSupplier)

        // 将创建出来的Collection集合返回
        if (resultSupplier.isSupplied()) {
            return resultSupplier.get()
        }
        return null
    }
}