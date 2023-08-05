package com.wanna.boot.origin

import com.wanna.framework.lang.Nullable

/**
 * 用于去包装value和Origin的Wrapper
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 */
open class OriginTrackedValue(private val value: Any, @Nullable private val origin: Origin?) : OriginProvider {

    /**
     * 获取Origin
     *
     * @return Origin
     */
    @Nullable
    override fun getOrigin(): Origin? = origin

    /**
     * 获取Value
     *
     * @return tracked value
     */
    open fun getValue(): Any = this.value

    /**
     * hashCode, 采用value去进行生成
     */
    override fun hashCode(): Int = this.value.hashCode()

    override fun equals(@Nullable other: Any?): Boolean {
        if (other === null || this.javaClass != other.javaClass) {
            return false
        }
        return this.value == (other as OriginTrackedValue).value
    }

    companion object {

        /**
         * 根据value快速构建出来OriginTrackedValue的工厂方法(origin=null)
         *
         * @param value value
         * @return OriginTrackedValue(or null)
         */
        @JvmStatic
        @Nullable
        fun of(@Nullable value: Any?): OriginTrackedValue? = of(value, null)

        /**
         * 根据value和origin构建OriginTrackedValue的工厂方法
         *
         * @param value value
         * @return OriginTrackedValue(or null)
         */
        @JvmStatic
        @Nullable
        fun of(@Nullable value: Any?, @Nullable origin: Origin?): OriginTrackedValue? {
            value ?: return null
            if (value is CharSequence) {
                return OriginTrackedCharSequence(value, origin)
            }
            return OriginTrackedValue(value, origin)
        }
    }

    /**
     * 字符串的OriginTrackedValue
     *
     * @param value value
     * @param origin origin
     */
    private class OriginTrackedCharSequence(value: CharSequence, @Nullable origin: Origin?) :
        OriginTrackedValue(value, origin),
        CharSequence {
        override val length = getValue().length

        override fun get(index: Int) = getValue()[index]
        override fun subSequence(startIndex: Int, endIndex: Int) = getValue().subSequence(startIndex, endIndex)

        override fun getValue(): CharSequence = super.value as CharSequence
    }
}