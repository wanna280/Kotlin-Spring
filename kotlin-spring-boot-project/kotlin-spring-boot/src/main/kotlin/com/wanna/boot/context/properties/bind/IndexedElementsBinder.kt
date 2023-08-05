package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 为IndexedElement(Array/Collection)提供绑定功能的[AggregateBinder],
 * 为[ArrayBinder]和[CollectionBinder]的实现, 去提供了足够多的模板方法, 去供子类所使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @see ArrayBinder
 * @see CollectionBinder
 *
 * @param context Binder内部用于属性绑定的上下文信息
 */
abstract class IndexedElementsBinder<T : Any>(context: Binder.Context) : AggregateBinder<T>(context) {

    /**
     * 对于一个IndexedElement(Array/Collection), 尝试使用所有的[ConfigurationPropertySource]去提供绑定功能
     *
     * @param name 要去进行绑定的属性Key前缀
     * @param target 要去进行绑定的目标元素
     * @param aggregateElementBinder 聚合元素的Binder
     * @param aggregateType 聚合类型(Collection/Array)
     * @param elementType 单个元素的类型(比如List<User>, 那么elementType就是User)
     * @param indexedCollectionSupplier 提供集合的单例对象的获取的功能
     */
    protected fun bindIndexed(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder,
        aggregateType: ResolvableType,
        elementType: ResolvableType,
        indexedCollectionSupplier: IndexedCollectionSupplier
    ) {
        context.getSources().forEach {
            // 通过当前的ConfigurationPropertySource尝试去进行绑定...
            bindIndexed(it, name, target, aggregateElementBinder, aggregateType, elementType, indexedCollectionSupplier)

            // 如果通过当前的ConfigurationPropertySource, 去绑定成功了, 那么直接返回
            if (indexedCollectionSupplier.isSupplied() && (indexedCollectionSupplier.get() as Collection<Any>?) != null) {
                return
            }
        }
    }

    /**
     * 针对单个[ConfigurationPropertySource], 尝试去进行绑定
     *
     * @param source 正在去进行尝试的[ConfigurationPropertySource]
     * @param root 要去进行绑定的属性Key前缀
     * @param target 要去进行绑定的目标元素
     * @param aggregateElementBinder 聚合元素的Binder
     * @param aggregateType 聚合类型(Collection/Array)
     * @param elementType 单个元素的类型(比如List<User>, 那么elementType就是User)
     * @param indexedCollectionSupplier 提供集合的单例对象的获取的功能
     */
    private fun bindIndexed(
        source: ConfigurationPropertySource,
        root: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder,
        aggregateType: ResolvableType,
        elementType: ResolvableType,
        indexedCollectionSupplier: IndexedCollectionSupplier
    ) {
        // 根据当前的属性前缀Key, 去直接获取属性值...
        val property = source.getConfigurationProperty(root)

        // 根据属性Key前缀可以直接获取到的话, 那么直接根据该属性值去进行绑定
        if (property != null) {
            // 设置正在进行绑定的ConfigurationProperty
            context.setConfigurationProperty(property)
            // 直接利用获取到的属性值, 去执行真正的绑定...(比如针对于"1,2,3"这样的配置的方式, 就可以在这里去进行解析)
            bindValue(target, indexedCollectionSupplier.get(), aggregateType, elementType, property.value)

            // 如果通过属性Key无法直接获取到, 那么就得尝试加上index, 后缀拼接上"[0]"/"[1]"/"[2]"再从source当中去进行获取并进行绑定...
        } else {
            bindIndexed(source, root, aggregateElementBinder, indexedCollectionSupplier, elementType)
        }
    }

    /**
     * 直接利用目标属性值(比如"1,2,3"这种情况), 去对Collection去进行绑定
     *
     * @param target 要去进行绑定的目标集合的相关信息
     * @param collection 要去进行收集最终结果的集合
     * @param aggregateType 聚合元素的类型(Collection/Array)
     * @param elementType 单个元素的类型(比如List<User>, 那么elementType就是User)
     * @param value 待去进行解析和转换成为一个Element的原始属性值
     */
    private fun bindValue(
        target: Bindable<Any>,
        collection: MutableCollection<Any>,
        aggregateType: ResolvableType,
        elementType: ResolvableType,
        @Nullable value: Any?
    ) {
        // 如果值为空, 那么pass, 直接使用空集合作为绑定结果
        if (value == null || (value is String && value.isBlank())) {
            return
        }

        // 将目标PropertyValue去转换成为集合列表(这里是把字符串去转换成为Collection/Array)
        val convertedValue = convert<Any>(value, aggregateType, target.annotations)

        // 再对得到的目标聚合元素(比如Array), 去转换成为Collection, 并加入到给定的collection列表当中去
        val collectionType =
            ResolvableType.forClassWithGenerics(collection::class.java, elementType.resolve(Any::class.java))
        val elements = convert<Collection<Any>>(convertedValue, collectionType, emptyArray()) ?: return

        // 添加到Collection当中去
        collection.addAll(elements)
    }

    /**
     * 将给定的value值去转换成为目标类型
     *
     * @param value value
     * @param targetType targetType
     * @param annotations 绑定需要用到的注解的相关信息
     * @return 转换之后得到的属性值(可能为null)
     */
    @Nullable
    private fun <C : Any> convert(value: Any?, targetType: ResolvableType, annotations: Array<Annotation>): C? {
        val resolvedValue = context.getPlaceholderResolver().resolvePlaceholder(value) ?: return null
        return context.getConverter().convert(resolvedValue, targetType, annotations)
    }

    /**
     * 根据index, 从[ConfigurationPropertySource]当中去获取元素去进行绑定
     *
     * @param source ConfigurationPropertySource
     * @param root 属性Key前缀
     * @param aggregateElementBinder 聚合元素的Binder, 是一个Callback回调函数, 用于去回调Binder, 去进行递归的绑定
     * @param indexedCollectionSupplier 最终的集合的Supplier
     * @param elementType 集合当中的元素类型
     */
    private fun bindIndexed(
        source: ConfigurationPropertySource,
        root: ConfigurationPropertyName,
        aggregateElementBinder: AggregateElementBinder,
        indexedCollectionSupplier: IndexedCollectionSupplier,
        elementType: ResolvableType
    ) {
        // 从0到MAX, 一个个尝试去进行属性的绑定
        for (index in 0 until Int.MAX_VALUE) {
            // 给root的属性Key前缀, 拼接上"[0]"/"[1]"这样的前缀..., 尝试从source当中去进行绑定
            val aggregatePropertyName = root.append("[$index]")

            // 使用AggregateElementBinder去对集合当中的一个元素进行绑定, 这个元素当然也是支持是复杂对象的,
            // 这里会回调AggregateElementBinder的bind方法, 从而去回调Binder.bind方法, 从而去对复杂对象去进行绑定...
            // 比如List<User>, 这里就会根据"xxx.user[0]"作为前缀, 去对单个User元素去提供递归绑定的功能...

            // 如果绑定结果为null, 直接pass掉...就不再去进行后续的继续绑定
            val boundValue = aggregateElementBinder.bind(aggregatePropertyName, Bindable.of(elementType), source)
                ?: return

            // 将绑定的集合的元素, 去收集到最终的result的集合当中去...
            indexedCollectionSupplier.get().add(boundValue)
        }
    }

    /**
     * Indexed Collection的Supplier
     *
     * @param supplier 提供Indexed Collection的实例化的Supplier
     */
    protected open class IndexedCollectionSupplier(supplier: Supplier<MutableCollection<Any>>) :
        AggregateSupplier<MutableCollection<Any>>(supplier)
}