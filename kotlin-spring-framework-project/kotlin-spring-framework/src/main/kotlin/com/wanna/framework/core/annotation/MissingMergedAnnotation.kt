package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
class MissingMergedAnnotation<A : Annotation> : AbstractMergedAnnotation<A>() {

    companion object {
        @JvmField
        val INSTANCE = MissingMergedAnnotation<Annotation>()

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <A : Annotation> getInstance(): MergedAnnotation<A> = this.INSTANCE as MergedAnnotation<A>
    }

    override val distance: Int
        get() = -1
    override val type: Class<A>
        get() = throw NoSuchElementException("Unable to get type for missing annotation")

    /**
     * 对missing的MergedAnnotation来说, 应该是不存在的, present=false
     */
    override val present: Boolean
        get() = false

    override val root: MergedAnnotation<*>
        get() = this

    /**
     * 对于missing, 不支持创建合成注解
     */
    override fun createSynthesized() = throw NoSuchElementException("Unable to synthesize missing annotation")

    override fun hasDefaultValue(attributeName: String) =
        throw NoSuchElementException("Unable to check default value for missing annotation")

    @Nullable
    override fun <T> getAttributeValue(attributeName: String, type: Class<T>) =
        throw NoSuchElementException("Unable to get attribute value for missing annotation")

    override fun toString(): String = "(missing)"
}