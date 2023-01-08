package com.wanna.boot.context.config

import com.wanna.boot.origin.Origin
import com.wanna.boot.origin.OriginProvider
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils

/**
 * 描述的是一个要去搜索配置文件的路径
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class ConfigDataLocation(val optional: Boolean, val value: String, @Nullable private val origin: Origin?) :
    OriginProvider {

    /**
     * 获取Origin
     *
     * @return Origin
     */
    @Nullable
    override fun getOrigin(): Origin? = origin

    /**
     * 检查当前Location当中是否是以给定的prefix作为前缀的?
     *
     * @param prefix 要去进行检查的prefix
     * @return 如果value以prefix作为开头, return true; 否则return false
     */
    open fun hasPrefix(prefix: String): Boolean = this.value.startsWith(prefix)

    /**
     * 如果当前Location当中以给定的prefix作为开头, 那么切割掉prefix
     *
     * @param prefix prefix
     * @return 如果value以prefix作为开头, return value去掉prefix之后的结果; 否则return value
     */
    open fun getNonPrefixedValue(prefix: String): String =
        if (hasPrefix(prefix)) value.substring(prefix.length) else value

    /**
     * 对于ConfigDataLocation的value, 使用";"去进行分割, 得到多个ConfigDataLocation
     *
     * @return 拆分之后得到的多个ConfigDataLocation
     */
    open fun split(): Array<ConfigDataLocation> = split(";")

    /**
     * 对于ConfigDataLocation的value, 使用给定的delim作为分隔符去进行分割, 得到多个ConfigDataLocation
     *
     * @param delim 分隔符
     * @return 拆分之后得到的多个ConfigDataLocation
     */
    open fun split(delim: String): Array<ConfigDataLocation> {
        val values = StringUtils.commaDelimitedListToStringArray(toString(), delim)
        return Array(values.size) { of(values[it])!!.withOrigin(origin) }
    }

    /**
     * 为ConfigDataLocation加上Origin, 并返回一个新的ConfigDataLocation对象
     *
     * @param origin Origin
     * @return 新的包含了Origin的ConfigDataLocation对象
     */
    open fun withOrigin(@Nullable origin: Origin?): ConfigDataLocation =
        ConfigDataLocation(this.optional, this.value, origin)

    /**
     * toString, 如果是Optional的话, 那么拼接上前缀
     *
     * @return toString
     */
    override fun toString(): String = if (optional) OPTIONAL_PREFIX + value else value

    companion object {

        /**
         * 识别这个ConfigDataResource是否是可选的?
         */
        const val OPTIONAL_PREFIX = "optional:"

        /**
         * 根据给定的Location, 去构建ConfigDataLocation
         *
         * @param location location
         * @return ConfigDataLocation(如果给定的location为null或者是空字符串, 那么return null)
         */
        @Nullable
        @JvmStatic
        fun of(@Nullable location: String?): ConfigDataLocation? {
            val optional = location != null && location.startsWith(OPTIONAL_PREFIX)
            val value = if (optional) location!!.substring(OPTIONAL_PREFIX.length) else location
            if (!StringUtils.hasText(value)) {
                return null
            }
            return ConfigDataLocation(optional, value!!, null)
        }
    }
}