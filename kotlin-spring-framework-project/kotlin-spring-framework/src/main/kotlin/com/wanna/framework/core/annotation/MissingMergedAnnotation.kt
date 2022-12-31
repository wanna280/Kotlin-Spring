package com.wanna.framework.core.annotation

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
        get() = TODO("Not yet implemented")
    override val type: Class<A>
        get() = TODO("Not yet implemented")
    override val present: Boolean
        get() = TODO("Not yet implemented")

    override val root: MergedAnnotation<*>
        get() = TODO("Not yet implemented")

    override fun createSynthesized(): A {
        TODO("Not yet implemented")
    }

    override fun hasDefaultValue(attributeName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> getAttributeValue(attributeName: String, type: Class<T>): T? {
        TODO("Not yet implemented")
    }
}