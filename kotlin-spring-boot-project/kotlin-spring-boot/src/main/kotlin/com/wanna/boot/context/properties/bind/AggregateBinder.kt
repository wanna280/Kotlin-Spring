package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import java.util.function.Supplier

/**
 * 提供聚合的策略的绑定(例如Map/List/Array), 供[Binder]去进行内部使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @param context Binder去进行属性绑定时用到的上下文信息
 * @param T 支持去进行处理的聚合元素的类型
 */
abstract class AggregateBinder<T : Any>(val context: Binder.Context) {

    /**
     * 是否允许去进行递归绑定
     *
     * @param source ConfigurationPropertySource
     * @return 如果允许递归绑定return true; 否则return false
     */
    open fun isAllowRecursiveBinding(source: ConfigurationPropertySource?): Boolean = false

    /**
     * 利用[AggregateElementBinder]去对一个聚合的元素去进行绑定
     *
     * @param name 属性Key前缀
     * @param target 要去进行绑定的目标元素(Map/Collection/Array)
     * @param aggregateElementBinder 提供聚合元素的绑定的Binder的回调函数
     * @return 绑定完成的实例对象(或者null)
     */
    fun bind(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder
    ): Any? {
        return bindAggregate(name, target, aggregateElementBinder)
    }

    /**
     * 执行聚合元素的绑定
     *
     * @param name 属性Key前缀
     * @param target 要去进行绑定的目标聚合元素的相关信息
     * @param aggregateElementBinder 提供聚合元素的绑定的Binder的回调函数
     */
    protected abstract fun bindAggregate(
        name: ConfigurationPropertyName,
        target: Bindable<Any>,
        aggregateElementBinder: AggregateElementBinder
    ): Any?

    /**
     * 聚合的实例的Supplier, 提供对于Supplier的结果的缓存, 保证多次调用, 返回的是相同的单例对象
     *
     * @param factory Supplier
     */
    protected open class AggregateSupplier<T : Any>(private val factory: Supplier<T>) : Supplier<T> {

        /**
         * 已经使用factory完成实例化的实例对象
         */
        private var supplied: T? = null

        /**
         * 检查实例对象是否已经通过factory完成了实例化?
         *
         * @return 如果已经完成实例化, return true; 否则return false
         */
        open fun isSupplied(): Boolean = this.supplied != null

        override fun get(): T {
            if (this.supplied == null) {
                this.supplied = factory.get()
            }
            return this.supplied!!
        }
    }
}