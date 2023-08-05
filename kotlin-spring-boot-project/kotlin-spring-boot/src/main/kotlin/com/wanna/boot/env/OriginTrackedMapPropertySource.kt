package com.wanna.boot.env

import com.wanna.boot.origin.Origin
import com.wanna.boot.origin.OriginLookup
import com.wanna.boot.origin.OriginTrackedValue
import com.wanna.framework.core.environment.MapPropertySource
import com.wanna.framework.lang.Nullable

/**
 * 具有Origin的追踪的PropertySource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param name PropertySource name
 * @param source PropertySource
 */
open class OriginTrackedMapPropertySource(
    name: String,
    source: Map<String, Any>,
    private val immutable: Boolean = false
) : MapPropertySource(name, source), OriginLookup<String> {
    constructor(name: String, source: Map<String, Any>) : this(name, source, false)

    /**
     * 重写父类的getProperty方法, 如果Value是OriginTrackedValue的话, 返回包装的value
     *
     * @param name name
     * @return PropertyValue(or null)
     */
    @Nullable
    override fun getProperty(name: String): Any? {
        val property = super.getProperty(name)
        if (property is OriginTrackedValue) {
            return property.getValue()
        }
        return property
    }

    /**
     * 通过key去获取到对应的PropertyValue的Origin
     *
     * @param key key
     * @return Origin(or null)
     */
    @Nullable
    override fun getOrigin(key: String): Origin? {
        val property = getProperty(key)
        if (property is OriginTrackedValue) {
            return property.getOrigin()
        }
        // 如果不是OriginTracedValue, return null
        return null
    }

    override fun isImmutable() = this.immutable
}