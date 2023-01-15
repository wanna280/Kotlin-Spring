package com.wanna.framework.web.mvc.condition

/**
 * 抽象的RequestCondition的实现
 *
 * @param T Condition类型, 采用递归定义泛型的方式, 让父类可以知道子类的泛型类型(子类指定自己的泛型类型)
 */
abstract class AbstractRequestCondition<T : AbstractRequestCondition<T>> : RequestCondition<T> {

    open fun isEmpty(): Boolean = getContent().isEmpty()

    /**
     * 获取该Condition要去进行匹配的条件列表
     *
     * @return 条件列表(比如请求方式列表)
     */
    abstract fun getContent(): Collection<*>

    /**
     * 获取描述的分隔符, 比如有多个元素, 在toString时, 需要采用分隔符, 而这个方法就是获取分隔符; 
     * 通常情况下, 分隔符主要是介绍的多个元素之间的逻辑关系, 比如header/param就必须是"&&", 而method就是"||"
     *
     * @return 想要使用的分隔符
     */
    abstract fun getToStringInfix(): String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractRequestCondition<*>) return false
        return other.getContent() == other.getContent()
    }

    override fun hashCode() = getContent().hashCode()
    override fun toString() = getContent().joinToString(separator = getToStringInfix(), prefix = "[", postfix = "]")
}