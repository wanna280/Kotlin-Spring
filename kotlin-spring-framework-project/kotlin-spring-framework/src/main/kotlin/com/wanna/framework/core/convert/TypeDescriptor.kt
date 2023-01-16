package com.wanna.framework.core.convert

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.io.Serializable
import java.lang.reflect.Field

/**
 * Spring针对于转换工作, 去封装的一个类型描述符, 主要新增泛型的支持;
 * 如果使用Class的方式去进行解析, 那么无法获取到泛型的类型, 但是如果使用ResolvableType, 则支持去进行泛型解析;
 *
 * 比如解析"Collection<String> --> List<String>", 就需要用到泛型的相关的支持
 *
 * @param resolvableType ResolvableType(with Generics if Necessary)
 *
 * @see ConversionService.canConvert
 * @see ConversionService.convert
 */
open class TypeDescriptor(val resolvableType: ResolvableType) :
    Serializable {

    constructor(property: Property) : this(ResolvableType.forClass(property.type))

    val type: Class<*> = resolvableType.resolve(Any::class.java)

    /**
     * 获取集合/数组元素类型的描述符
     *
     * @return 元素类型的[TypeDescriptor]
     */
    @Nullable
    open fun getElementTypeDescriptor(): TypeDescriptor? {
        if (type.isArray) {
            return TypeDescriptor(ResolvableType.forClass(type.componentType))
        } else if (ClassUtils.isAssignFrom(Collection::class.java, type)) {
            return TypeDescriptor(resolvableType.asCollection().getGenerics()[0])
        }
        return null
    }

    companion object {

        @JvmStatic
        private val CACHED_COMMON_TYPES = arrayOf(
            Byte::class.java, Byte::class.javaObjectType,
            Boolean::class.java, Boolean::class.javaObjectType,
            Short::class.java, Short::class.javaObjectType,
            Int::class.java, Int::class.javaObjectType,
            Long::class.java, Long::class.javaObjectType,
            Double::class.java, Long::class.javaObjectType,
            Char::class.java, Char::class.javaObjectType,
            Float::class.java, Float::class.javaObjectType,
            String::class.java, Any::class.java
        )

        /**
         * 常用类型的TypeDescriptor缓存
         */
        @JvmStatic
        private val commonTypesCache = LinkedHashMap<Class<*>, TypeDescriptor>()

        init {
            // 为所有的要去进行缓存的类型, 去构建出来缓存
            for (type in CACHED_COMMON_TYPES) {
                commonTypesCache[type] = valueOf(type)
            }
        }

        /**
         * 构建[TypeDescriptor]的工厂方法
         *
         * @param clazz 要去进行获取[TypeDescriptor]的类
         * @return 为该类去获取到的[TypeDescriptor]描述信息
         */
        @JvmStatic
        fun valueOf(@Nullable clazz: Class<*>?): TypeDescriptor {
            val type = clazz ?: Any::class.java

            // 如果是基础数据类型, 直接走缓存去进行获取
            val desc = commonTypesCache[type]
            return desc ?: TypeDescriptor(ResolvableType.forClass(type))
        }

        @JvmStatic
        fun forMethodParameter(parameter: MethodParameter): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forMethodParameter(parameter))
        }

        @JvmStatic
        fun forField(field: Field): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forField(field))
        }

        /**
         * 为给定的类去获取到[TypeDescriptor]
         *
         * @param type type
         * @return TypeDescriptor
         */
        @JvmStatic
        fun forClass(type: Class<*>): TypeDescriptor = valueOf(type)

        /**
         * 为给定的对象的类去获取到[TypeDescriptor]
         *
         * @param source 要去进行描述的目标对象
         * @return TypeDescriptor
         */
        @JvmStatic
        fun forObject(source: Any): TypeDescriptor = valueOf(source.javaClass)

        @JvmStatic
        fun forClassWithGenerics(clazz: Class<*>, vararg generics: Class<*>): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forClassWithGenerics(clazz, *generics))
        }
    }

}