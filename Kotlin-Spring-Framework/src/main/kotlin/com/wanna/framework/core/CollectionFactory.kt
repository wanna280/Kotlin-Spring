package com.wanna.framework.core

import com.wanna.framework.core.util.ClassUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

/**
 * CollectionFactory，支持去创建Collection
 */
object CollectionFactory {

    /**
     * 创建一个和指定的Collection最接近的Collection
     *
     * @param collectionType 想要匹配的Collection类型
     * @param capacity 容量
     * @return 创建的最接近的Collection类型
     */
    @JvmStatic
    fun <E> createApproximateCollection(collectionType: Class<*>, capacity: Int): MutableCollection<E> {
        if (ClassUtils.isAssignFrom(LinkedList::class.java, collectionType)) {
            return LinkedList()
        }
        if (ClassUtils.isAssignFrom(List::class.java, collectionType)) {
            return java.util.ArrayList()
        }
        if (ClassUtils.isAssignFrom(SortedSet::class.java, collectionType)) {
            return TreeSet()
        }
        return java.util.LinkedHashSet()
    }

    /**
     * 给定一个具体的Collection，去创建一个Collection
     *
     * @param collectionType CollectionType
     * @param capacity 容量
     * @return 创建好的Collection
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <E> createCollection(collectionType: Class<*>, capacity: Int): MutableCollection<E> {
        if (collectionType.isInterface) {
            if (collectionType == Set::class.java || collectionType == Collection::class.java) {
                return LinkedHashSet(capacity)
            }
            if (collectionType == List::class.java) {
                return ArrayList(capacity)
            }
            if (collectionType == NavigableSet::class.java || collectionType == SortedSet::class.java) {
                return TreeSet()
            }
            throw IllegalArgumentException("不支持去创建这样的集合[type=$collectionType]")
        }
        return ClassUtils.newInstance(collectionType) as MutableCollection<E>
    }
}