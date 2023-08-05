package com.wanna.boot.context.properties.bind

import com.wanna.framework.core.ResolvableType
import java.util.function.Supplier

/**
 * 用于去对一个JavaBean去完成属性的绑定的Bindable对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param type beanType
 * @param annotations 可能影响属性的绑定的注解信息
 * @param value 用于去对Bean去进行实例化的Supplier(可以为null)
 */
class Bindable<T : Any>(
    val type: ResolvableType,
    val annotations: Array<Annotation>,
    val value: Supplier<T>?
) {

    /**
     * 从annotations列表当中去找到一个和给定的注解类型匹配的一个注解
     *
     * @param type type
     * @return 根据type去获取到的注解对象(获取不到的话, return null)
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Annotation> getAnnotation(type: Class<A>): A? {
        for (annotation in annotations) {
            if (type.isInstance(annotation)) {
                return annotation as A
            }
        }
        return null;
    }

    /**
     * 根据注解信息, 去获取到一个更加完成的Bindable对象
     *
     * @param annotations 注解信息
     * @return 得到的新的更加完整的Bindable
     */
    fun withAnnotations(vararg annotations: Annotation): Bindable<T> {
        return Bindable(type, if (annotations.isEmpty()) NO_ANNOTATIONS else arrayOf(*annotations), value)
    }

    /**
     * 根据一个已经存在的Java对象, 去获取到一个更加完整的Bindable对象
     *
     * @param value 已经存在的Java对象
     * @return 得到的新的更加完整的Bindable
     */
    fun withExistingValue(value: T): Bindable<T> {
        return withSuppliedValue { value }
    }

    /**
     * 根据一个用于实例化对象的Supplier, 去获取到一个更加完整的Bindable对象
     *
     * @param value Supplier
     * @return 得到的新的更加完成的Bindable
     */
    fun withSuppliedValue(value: Supplier<T>?): Bindable<T> {
        return Bindable(type, annotations, value)
    }

    override fun toString(): String {
        return "Bindable(type=$type, value=${if (value == null) "none" else "provided"})"
    }


    companion object {

        /**
         * 没有注解的常量标识
         */
        @JvmField
        val NO_ANNOTATIONS: Array<Annotation> = arrayOf()

        /**
         * 根据一个已经存在的Java对象, 去构建出来Bindable
         *
         * @param instance 实例对象
         * @return Bindable
         */
        @JvmStatic
        fun <T : Any> ofInstance(instance: T): Bindable<T> {
            return of(instance.javaClass).withExistingValue(instance)
        }

        /**
         * 根据一个Class去构建出来Bindable
         *
         * @param type BeanType
         * @return Bindable
         */
        @JvmStatic
        fun <T : Any> of(type: Class<T>): Bindable<T> {
            return of(ResolvableType.forClass(type))
        }

        /**
         * 根据一个ResolvableType去构建出来Bindable
         *
         * @param type type
         * @return Bindable
         */
        @JvmStatic
        fun <T : Any> of(type: ResolvableType): Bindable<T> {
            return Bindable(type, NO_ANNOTATIONS, null)
        }

        /**
         * 根据List的元素类型, 去快速构建出来一个类型为List<E>的Bindable
         *
         * @param elementType list的元素类型
         * @return beanType为List<E>的Bindable
         */
        @JvmStatic
        fun <E : Any> listOf(elementType: Class<E>): Bindable<List<E>> {
            return of(ResolvableType.forClassWithGenerics(List::class.java, elementType))
        }

        /**
         * 根据Set的元素类型, 去快速构建出来一个类型为Set<T>的Bindable
         *
         * @param elementType Set的元素类型
         * @return beanType为Set<T>的Bindable
         */
        @JvmStatic
        fun <E : Any> setOf(elementType: Class<E>): Bindable<Set<E>> {
            return of(ResolvableType.forClassWithGenerics(Set::class.java, elementType))
        }

        /**
         * 根据Map的元素类型, 去快速构建出来一个类型为Map<K, V>的Bindable
         *
         * @param keyType key的元素类型
         * @param valueType value的元素类型
         * @return beanType为Map<K, V>的Bindable
         */
        @JvmStatic
        fun <K : Any, V : Any> mapOf(keyType: Class<K>, valueType: Class<V>): Bindable<Map<K, V>> {
            return of(ResolvableType.forClassWithGenerics(Map::class.java, keyType, valueType))
        }
    }
}