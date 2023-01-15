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
        fun forMethodParameter(parameter: MethodParameter): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forMethodParameter(parameter))
        }

        @JvmStatic
        fun forField(field: Field): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forField(field))
        }

        @JvmStatic
        fun forClass(type: Class<*>): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forClass(type))
        }

        @JvmStatic
        fun forObject(source: Any): TypeDescriptor {
            return forClass(source.javaClass)
        }

        @JvmStatic
        fun forClassWithGenerics(clazz: Class<*>, vararg generics: Class<*>): TypeDescriptor {
            return TypeDescriptor(ResolvableType.forClassWithGenerics(clazz, *generics))
        }
    }

}