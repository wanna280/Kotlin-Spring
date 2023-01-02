package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import com.wanna.framework.core.KotlinDetector
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.annotation.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * 针对ValueObject的DataObjectBinder, 提供基于构造器的方式去进行绑定
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 *
 * @param bindConstructorProvider 提供要去进行绑定的构造器的Provider
 */
open class ValueObjectBinder(private val bindConstructorProvider: BindConstructorProvider) : DataObjectBinder {

    /**
     * 基于构造器的方式, 去执行对于一个ValueObject的属性的真正绑定
     *
     * @param name 要去将属性去绑定到当前的ValueObject上的前缀信息
     * @param target 要去进行绑定的目标ValueObject的相关信息
     * @param context context
     * @param propertyBinder PropertyBinder回调, 执行这个PropertyBinder执行对于单个的属性的绑定
     * @return 获取到要去进行绑定的Java对象(如果return null, 代表绑定失败)
     */
    override fun <T : Any> bind(
        name: ConfigurationPropertyName,
        target: Bindable<T>,
        context: Binder.Context,
        propertyBinder: DataObjectPropertyBinder
    ): T? {
        // 根据给定的要去进行绑定的信息, 构建出来ValueObject对象(如果它不是一个ValueObject的话, 直接return null, 绑定失败)
        val valueObject = ValueObject.get(target, bindConstructorProvider, context) ?: return null

        // 在绑定构造器参数之前, push ConstructorBinding Stack
        context.pushConstructorBoundTypes(target.type.resolve(Any::class.java))

        // 获取到该ValueObject的构造器参数信息
        val constructorParameters = valueObject.constructorParameters
        // 构造器的参数列表
        val args = ArrayList<Any?>(constructorParameters.size)

        // 统计一下, 是否已经执行了真正的绑定? 只要其中一个参数进行过绑定, 那么就设置为true
        var bound = false

        // 对于所有的构造器参数, 去执行挨个的绑定...
        constructorParameters.forEach {
            val arg = it.bind(propertyBinder)
            bound = bound || arg != null
            // 将当前的属性值的绑定结果, 添加到args当中
            args.add(arg)
        }

        // 在执行构造器的绑定之后, 需要清除相关的信息
        // clear ConfigurationProperty
        context.clearConfigurationProperty()
        // pop ConstructorBinding Stack
        context.popConstructorBoundTypes()

        // 利用ValueObject, 以及给定的构造器参数, 去实例化ValueObject对象
        // 特殊地: 对于一个属性都没成功完成绑定的话, return null; 让它去尝试下一个DataObjectBinder去进行绑定
        return if (bound) valueObject.instantiate(args) else null
    }

    /**
     * 对于对象不存在时, 去进行创建对象时的方式
     *
     * @param target 要去进行绑定的目标元素的相关信息
     * @param context BindContext
     * @return 创建完成的目标ValueObject对象(or null)
     */
    @Nullable
    override fun <T : Any> create(target: Bindable<T>, context: Binder.Context): T? {
        // 根据给定的要去进行绑定的信息, 构建出来ValueObject对象
        val valueObject = ValueObject.get(target, bindConstructorProvider, context) ?: return null

        // 获取到该ValueObject的构造器参数信息
        val constructorParameters = valueObject.constructorParameters
        // 构造器的参数列表
        val args = ArrayList<Any?>(constructorParameters.size)

        // 利用ValueObject, 以及给定的构造器参数, 去实例化ValueObject对象
        return valueObject.instantiate(args)
    }

    /**
     * 对于ValueObject的抽象
     *
     * @param T ValueObject的类型
     *
     * @param constructor 对于实例化ValueObject对象时需要用到的构造器Constructor
     */
    abstract class ValueObject<T : Any>(private val constructor: Constructor<T>) {

        /**
         * 根据给定的参数列表, 使用构造器去完成实例化, 得到Java对象
         *
         * @param args 构造器的参数列表
         * @return 使用有参数构造器去进行实例化得到的ValueObject实例对象
         */
        fun instantiate(args: List<Any?>): T = BeanUtils.instantiateClass(constructor, *args.toTypedArray())

        /**
         * 获取当前ValueObject构造器的参数列表信息, 这里使用Kotlin的抽象字段的方式去实现
         */
        abstract val constructorParameters: List<ConstructorParameter>

        companion object {

            /**
             * 提供对于构建ValueObject的工厂方法
             *
             * @param bindable bindable
             * @param constructorProvider 提供ValueObject的实例化时需要用到的构造器的Provider
             * @param context BindContext
             * @return 创建得到的ValueObject(or null)
             */
            @Nullable
            @Suppress("UNCHECKED_CAST")
            @JvmStatic
            fun <T : Any> get(
                bindable: Bindable<T>, constructorProvider: BindConstructorProvider, context: Binder.Context
            ): ValueObject<T>? {
                val type = bindable.type.resolve()
                // 对于枚举/抽象类, 它不是一个ValueObject, 在这里直接去return false, pass掉
                if (type == null || type.isEnum || Modifier.isAbstract(type.modifiers)) {
                    return null
                }

                // 使用Constructor去获取到ValueObject的实例化时需要使用到的构造器对象
                val constructor = constructorProvider.getBindConstructor(bindable, context.isNestedConstructorBinding())
                    ?: return null

                // 如果它是一个KotlinType, 尝试使用KotlinValueObject; 如果不是的话, 那么仍旧尝试DefaultValueObject的实例化方式
                if (KotlinDetector.isKotlinType(type)) {
                    return KotlinValueObject.get(constructor as Constructor<T>, bindable.type)
                }
                // 使用默认的DefaultValueObject实例方式
                return DefaultValueObject.get(constructor as Constructor<T>, bindable.type)
            }
        }
    }

    /**
     * 默认的ValueObject的实现方式
     *
     * @param constructor 构造器
     * @param type ValueObject的实例对象的类型
     */
    class DefaultValueObject<T : Any>(constructor: Constructor<T>, type: ResolvableType) : ValueObject<T>(constructor) {

        /**
         * 获取到当前ValueObject的构造器参数信息
         */
        override val constructorParameters = parseConstructorParameters(constructor, type)

        companion object {
            /**
             * 默认的参数名发现器, 提供对于构造器的参数名的发现
             */
            @JvmStatic
            private val PARAMETER_NAME_DISCOVERER = DefaultParameterNameDiscoverer()

            /**
             * 解析给定的构造器的参数信息, name&type&annotations
             *
             * @param constructor 构造器
             * @param type 对象类型
             */
            @JvmStatic
            private fun parseConstructorParameters(
                constructor: Constructor<*>, type: ResolvableType
            ): List<ConstructorParameter> {
                val names = PARAMETER_NAME_DISCOVERER.getParameterNames(constructor)
                    ?: throw IllegalStateException("Failed to extract parameter names for $constructor")
                val parameters = constructor.parameters
                val result = ArrayList<ConstructorParameter>()
                parameters.indices.forEach { index ->
                    // 解析当前位置的构造器的参数名, 如果有@Name注解的话, 使用给定的注解去作为parameterName, 否则使用参数名发现器解析得到的parameterName
                    val name = AnnotatedElementUtils.getMergedAnnotation(parameters[index], Name::class.java)?.value
                        ?: names[index]

                    // 构建出来当前构造器参数的相关信息(基于Constructor&index去进行构建)
                    val resolvableType = ResolvableType.forMethodParameter(MethodParameter(constructor, index))

                    // 构建出来当前构造器参数的注解信息
                    val annotations = parameters[index].declaredAnnotations

                    // 收集到result当中去
                    result += ConstructorParameter(name, resolvableType, annotations)
                }
                return Collections.unmodifiableList(result)
            }

            /**
             * 基于构造器&ResolvableType去构建DefaultValueObject的工厂方法
             *
             * @param constructor Constructor
             * @param type type
             * @return DefaultValueObject
             */
            @JvmStatic
            fun <T : Any> get(constructor: Constructor<T>, type: ResolvableType): ValueObject<T> {
                return DefaultValueObject(constructor, type)
            }
        }
    }

    /**
     * Kotlin的ValueObject的实现
     *
     * @param constructor constructor
     * @param kotlinConstructor Kotlin构造器的KFunction
     * @param type type
     */
    class KotlinValueObject<T : Any>(
        constructor: Constructor<T>, private val kotlinConstructor: KFunction<T>, private val type: ResolvableType
    ) : ValueObject<T>(constructor) {

        /**
         * 获取当前ValueObject构造器的参数列表信息
         */
        override val constructorParameters = parseConstructorParameters(kotlinConstructor, type)

        companion object {

            /**
             * 根据Kotlin的构造器的KFunction的参数, 去解析得到构造器参数信息
             *
             * @param kotlinConstructor Kotlin的KFunction
             * @param type type
             * @return 解析得到的构造器参数信息
             */
            @OptIn(ExperimentalStdlibApi::class)
            @JvmStatic
            private fun <T : Any> parseConstructorParameters(
                kotlinConstructor: KFunction<T>, type: ResolvableType
            ): List<ConstructorParameter> {
                val result = ArrayList<ConstructorParameter>()
                kotlinConstructor.parameters.forEach {
                    // 解析当前位置的构造器的参数名, 如果有@Name注解的话, 使用给定的注解去作为parameterName, 否则使用KParameter的parameterName
                    val name = getParameterName(it)
                    val paramType = ResolvableType.forType(it.type.javaType, type)
                    result.add(ConstructorParameter(name, paramType, it.annotations.toTypedArray()))
                }
                return result
            }

            /**
             * 获取到某个位置的构造器参数的参数名(如果有`@Name`注解的话, 那么使用给定的name; 不然使用Kotlin的KParameter的name)
             *
             * @param parameter Kotlin KParameter
             * @return 从给定的KParameter当中去解析得到的参数名
             */
            @JvmStatic
            private fun getParameterName(parameter: KParameter): String {
                val mergedAnnotation = MergedAnnotations.from(
                    parameter, parameter.annotations.toTypedArray(), RepeatableContainers.none(), AnnotationFilter.PLAIN
                ).get(Name::class.java)
                if (!mergedAnnotation.present) {
                    return parameter.name ?: ""
                }
                return mergedAnnotation
                    .getValue(MergedAnnotation.VALUE, String::class.java)
                    .orElse(parameter.name ?: "")
            }

            /**
             * 基于构造器&ResolvableType去构建KotlinValueObject的工厂方法
             *
             * @param constructor Constructor
             * @param type type
             * @return KotlinValueObject/DefaultValueObject
             */
            @JvmStatic
            fun <T : Any> get(constructor: Constructor<T>, type: ResolvableType): ValueObject<T> {
                val kotlinFunction = constructor.kotlinFunction

                // 如果可以获取到KotlinFunction的话, 那么return KotlinValueObject
                if (kotlinFunction != null) {
                    return KotlinValueObject(constructor, kotlinFunction, type)
                }

                // 获取不到的话, 仍旧使用默认的DefaultValueObject
                return DefaultValueObject.get(constructor, type)
            }
        }
    }

    /**
     * 对于一个构造器参数的封装(name&type&annotations)
     *
     * @param _name name
     * @param type type
     * @param annotations Annotations
     */
    class ConstructorParameter(
        _name: String, val type: ResolvableType, val annotations: Array<Annotation>
    ) {
        /**
         * 将name转换成为dash风格
         */
        private val name = DataObjectPropertyName.toDashedForm(_name)

        /**
         * 利用给定的DataObjectPropertyBinder, 去对这个构造器参数去执行绑定
         *
         * @param binder DataObjectPropertyBinder
         * @return 绑定得到的结果
         */
        fun bind(binder: DataObjectPropertyBinder): Any? {
            return binder.bindProperty(name, Bindable.of<Any>(type).withAnnotations(*annotations))
        }

        override fun toString(): String = "name=$name, type=$type, annotations=${annotations.contentToString()}"
    }
}