package com.wanna.framework.core

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ClassUtils.getQualifiedName
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import com.wanna.framework.util.ReflectionUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

/**
 * CollectionFactory，支持给定collectionType/mapType去创建Collection/Map的对象, 是一个工具类
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
     * 给定一个具体的CollectionType，去创建一个Collection实例对象
     *
     * @param collectionType CollectionType
     * @param capacity 容量
     * @return 创建好的Collection
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <E> createCollection(collectionType: Class<*>, capacity: Int): MutableCollection<E> {
        return createCollection(collectionType, null, capacity)
    }

    /**
     * 给定一个具体的CollectionType，去创建一个Collection实例对象
     *
     * @param collectionType CollectionType
     * @param capacity 预期容量
     * @param elementType elementType, 只有在EnumSet的情况下需要用到
     * @return 创建好的Collection
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <E> createCollection(collectionType: Class<*>, elementType: Class<*>?, capacity: Int): MutableCollection<E> {
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
            throw IllegalArgumentException("不支持去创建这样的集合[type=${getQualifiedName(collectionType)}]")
        } else if (collectionType == EnumSet::class.java) {
            elementType ?: throw IllegalArgumentException("EnumSet的情况下, elementType必须给定, 但是实际值为null")
            return EnumSet.noneOf(asEnumType(elementType)) as MutableCollection<E>

            // 如果给的不是一个Collection类型, 那么丢异常出来
        } else if (!ClassUtils.isAssignFrom(Collection::class.java, collectionType)) {
            throw IllegalArgumentException("给定的collectionType类型不对, type=[${getQualifiedName(collectionType)}]")
        } else {
            // 直接使用反射的方式, 用无参数构造器去进行实例化
            try {
                return ReflectionUtils.accessibleConstructor(collectionType).newInstance() as MutableCollection<E>
            } catch (ex: Throwable) {
                throw IllegalArgumentException("无法实例化collectionType[${getQualifiedName(collectionType)}]", ex)
            }
        }
    }

    /**
     * 给定一个具体的MapType, 去创建出来一个Map实例对象
     *
     * @param mapType mapType
     * @param capacity 预期初始容量
     * @return 根据mapType创建出来的Map
     */
    @JvmStatic
    fun <K, V> createMap(mapType: Class<*>, capacity: Int): MutableMap<K, V> {
        return createMap(mapType, null, capacity)
    }

    /**
     * 给定一个具体的MapType, 去创建出来一个Map实例对象
     *
     * @param mapType mapType
     * @param capacity 预期初始容量
     * @param keyType keyType(在EnumMap的情况下, 才需要使用到keyType)
     * @return 根据mapType创建出来的Map
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <K, V> createMap(mapType: Class<*>, @Nullable keyType: Class<*>?, capacity: Int): MutableMap<K, V> {
        if (mapType.isInterface) {
            if (mapType == Map::class.java) {
                return LinkedHashMap(capacity)
            } else if (mapType != SortedMap::class.java && mapType != NavigableMap::class.java) {

                // 如果给定的是一个MultiValueMap
                if (mapType == MultiValueMap::class.java) {
                    return LinkedMultiValueMap<K, Any>() as MutableMap<K, V>
                } else {
                    throw IllegalArgumentException("不支持给定的这样的Map接口[${getQualifiedName(mapType)}]")
                }
            } else {
                return TreeMap()
            }
        } else if (EnumMap::class.java == mapType) {
            keyType ?: throw IllegalArgumentException("对于实例化EnumMap, keyType是必须给定的, 但是实际给定的为null")
            // 这里本来可以直接使用EnumMap的构造器去进行创建, 但是Kotlin这里不给定V的泛型不让编译过, 不知道怎么给定V的泛型, 这里暂时使用泛型去创建
            // return EnumMap<Enum<*>, V>(asEnumType(keyType) as Class<Enum<*>>) as MutableMap<K, V>
            return ReflectionUtils.accessibleConstructor(EnumMap::class.java, Class::class.java)
                .newInstance(asEnumType(keyType)) as MutableMap<K, V>
        } else if (!ClassUtils.isAssignFrom(Map::class.java, mapType)) {
            throw IllegalArgumentException("给定的mapType, 并不是Map类型")
        } else {
            // 直接使用反射去使用无参数构造器进行实例化mapType
            try {
                return ReflectionUtils.accessibleConstructor(mapType).newInstance() as MutableMap<K, V>
            } catch (ex: Throwable) {
                throw IllegalArgumentException("无法实例化mapType=[${getQualifiedName(mapType)}]", ex)
            }
        }
    }

    /**
     * 将给定的type去转换成为枚举类型
     *
     * @param type type
     * @return 转换得到的枚举类
     * @throws IllegalArgumentException 如果给定的类不是一个合法的枚举类的话
     */
    @JvmStatic
    private fun asEnumType(type: Class<*>): Class<out Enum<*>> {
        if (ClassUtils.isAssignFrom(Enum::class.java, type)) {
            throw IllegalArgumentException("给定的type=[${getQualifiedName(type)}], 并不是一个Enum枚举类型")
        }
        return type.asSubclass(Enum::class.java)
    }
}