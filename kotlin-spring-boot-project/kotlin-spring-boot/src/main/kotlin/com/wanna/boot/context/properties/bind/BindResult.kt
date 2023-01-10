package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable
import java.util.function.Function
import java.util.function.Supplier
import kotlin.jvm.Throws

/**
 * 绑定属性值的结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class BindResult<T : Any>(@Nullable val value: T?) {


    /**
     * 获取绑定结果
     *
     * @return 绑定结果
     * @throws NoSuchElementException 如果绑定结果不存在的话
     */
    @Throws(NoSuchElementException::class)
    fun get(): T = this.value ?: throw NoSuchElementException("No value bound")

    /**
     * 当绑定结果的value为空情况下, 需要使用Supplier去进行返回默认值
     *
     * @param supplier supplier
     * @return bind result
     */
    fun orElseGet(supplier: Supplier<out T>): T = value ?: supplier.get()

    /**
     * 在绑定结果为空的情况下, 返回other作为默认值
     *
     * @return bind result value or other
     */
    @Nullable
    fun orElse(@Nullable other: T?): T? = value ?: other

    /**
     * 是否已经完成了绑定
     *
     * @return 如果已经完成绑定return true; 否则return false
     */
    fun isBound(): Boolean = this.value != null

    /**
     * 对于绑定结果, 执行map操作
     *
     * @param mapper 执行map操作的Function
     * @return map之后的BindResult
     */
    fun <U : Any> map(mapper: Function<T, U>): BindResult<U> {
        return of(if (this.value != null) mapper.apply(this.value) else null)
    }

    companion object {

        /**
         * 没有绑定的成功的BindResult常量对象
         */
        @JvmField
        val UNBOUND = BindResult<Any>(null)

        /**
         * 根据绑定完成的Java实例对象, 去构建得到绑定结果BindResult
         *
         * @param value 绑定完成的实例对象
         * @return BindResult
         */
        @JvmStatic
        fun <T : Any> of(@Nullable value: T?): BindResult<T> {
            return BindResult(value)
        }
    }
}