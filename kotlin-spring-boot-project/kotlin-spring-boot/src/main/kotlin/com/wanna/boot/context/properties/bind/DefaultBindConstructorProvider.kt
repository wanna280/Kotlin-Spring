package com.wanna.boot.context.properties.bind

import com.wanna.boot.context.properties.ConstructorBinding
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.core.KotlinDetector
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

/**
 * [BindConstructorProvider]的默认实现, 用于为一个要去进行绑定的元素去提供绑定的构造器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
open class DefaultBindConstructorProvider : BindConstructorProvider {

    /**
     * 为给定的要去进行绑定的元素, 去获取到合适的构造器
     *
     * @param bindable bindable
     * @param isNestedConstructorBinding 是否是嵌套的构造器绑定?
     * @return 获取到的用于去进行绑定的构造器(获取不到return null)
     */
    @Nullable
    override fun getBindConstructor(bindable: Bindable<*>, isNestedConstructorBinding: Boolean): Constructor<*>? {
        return getBindConstructor(bindable.type.resolve(), isNestedConstructorBinding)
    }

    /**
     * 为给定的要去进行绑定的元素, 去获取到合适的构造器
     *
     * @param type 要去进行绑定的对象的类型
     * @param isNestedConstructorBinding 是否是嵌套的构造器绑定?
     * @return 为type去获取到的用于去进行绑定的构造器(获取不到return null)
     */
    @Nullable
    private fun getBindConstructor(@Nullable type: Class<*>?, isNestedConstructorBinding: Boolean): Constructor<*>? {
        type ?: return null
        // 为给定的类去找到合适的用于去进行绑定的构造器
        val constructors = Constructors.getConstructors(type)

        // 如果有找到合适的要去进行绑定的构造器, 但是也有@Autowired标注的构造器, 那么丢出异常
        if (constructors.bind != null && isNestedConstructorBinding) {
            if (constructors.hasAutowired) {
                throw IllegalStateException("${type.name} declares @ConstructorBinding and @Autowired constructor")
            }
        }
        return constructors.bind
    }

    /**
     * 用于为目标类去找到合适的用于去进行绑定的构造器
     *
     * @param hasAutowired 是否存在有@Autowired注解标注的构造器?
     * @param bind 决策出来的最终的用于去进行绑定的构造器(可能为null)
     */
    class Constructors(val hasAutowired: Boolean, @Nullable val bind: Constructor<*>?) {

        companion object {

            /**
             * 构建[Constructors]的工厂方法
             *
             * @param type 要去进行构建的类
             * @return 对于该类的要去进行绑定的构造器的描述信息
             */
            @JvmStatic
            fun getConstructors(type: Class<*>): Constructors {
                val hasAutowiredConstructor = isAutowiredPresent(type)
                val candidateConstructors = getCandidateConstructors(type)
                val mergedAnnotations = getAnnotations(candidateConstructors)
                var bind = getConstructorBindingAnnotated(type, candidateConstructors, mergedAnnotations)
                if (bind == null && !hasAutowiredConstructor) {
                    bind = deduceBindConstructor(type, candidateConstructors)
                }
                if (bind == null && !hasAutowiredConstructor && isKotlinType(type)) {
                    bind = deduceKotlinBindConstructor(type)
                }
                return Constructors(hasAutowiredConstructor, bind)
            }

            /**
             * 推测绑定的构造器, 如果只有一个无参数构造器的话, return; 否则return null
             *
             * @param type type
             * @param constructors 候选的构造器列表
             * @return 从候选的构造器列表当中, 如果只有一个有参数构造器, return; 否则 return null
             */
            @JvmStatic
            private fun deduceBindConstructor(type: Class<*>, constructors: Array<Constructor<*>>): Constructor<*>? {
                if (constructors.size == 1 && constructors[0].parameterCount > 0) {
                    if (type.isMemberClass && Modifier.isPrivate(constructors[0].modifiers)) {
                        return null
                    }
                    return constructors[0]
                }
                var result: Constructor<*>? = null

                for (ctor in constructors) {
                    if (!Modifier.isPrivate(ctor.modifiers)) {
                        if (result != null) {
                            return null
                        }
                        result = ctor
                    }
                }
                return if (result != null && result.parameterCount > 0) result else null
            }

            /**
             * 检查给定的类是否是一个Kotlin的类?
             *
             * @param type type
             * @return 如果是Kotlin类, return true; 否则return false
             */
            @JvmStatic
            private fun isKotlinType(type: Class<*>): Boolean =
                KotlinDetector.isKotlinPresent() && KotlinDetector.isKotlinType(type)

            /**
             * 根据Kotlin类型去进行绑定构造器的推断
             *
             * @param type type
             * @return 如果有主构造器, return 主构造器; 否则return null
             */
            @Nullable
            @JvmStatic
            private fun deduceKotlinBindConstructor(type: Class<*>): Constructor<*>? {
                val primaryConstructor = BeanUtils.findPrimaryConstructor(type)
                if (primaryConstructor != null && primaryConstructor.parameterCount > 0) {
                    return primaryConstructor
                }
                return null
            }

            /**
             * 获取到标注有[ConstructorBinding]的构造器
             *
             * @param type type
             * @param candidates candidate constructors
             * @param mergedAnnotations merged annotations for constructors
             * @return 解析到的[ConstructorBinding]的构造器
             */
            @Nullable
            @JvmStatic
            private fun getConstructorBindingAnnotated(
                type: Class<*>,
                candidates: Array<Constructor<*>>,
                mergedAnnotations: Array<MergedAnnotations>
            ): Constructor<*>? {
                var result: Constructor<*>? = null
                for (index in candidates.indices) {
                    if (mergedAnnotations[index].isPresent(ConstructorBinding::class.java)) {
                        if (candidates[index].parameterCount == 0) {
                            throw IllegalStateException("${type.name} declares @ConstructorBinding on a no-args constructor")
                        }
                        if (result != null) {
                            throw IllegalStateException("${type.name} has more than one @ConstructorBinding constructor")
                        }
                        result = candidates[index]
                    }
                }
                return result
            }

            /**
             * 获取给定的所有的构造器当中的注解列表
             *
             * @param candidates 候选的构造器列表
             * @return 所有的构造器当中的注解信息
             */
            private fun getAnnotations(candidates: Array<Constructor<*>>): Array<MergedAnnotations> {
                return candidates.map(MergedAnnotations::from).toTypedArray()
            }

            /**
             * 从给定的类当中获取候选的构造器
             *
             * @param type type
             * @return 候选构造器列表
             */
            @JvmStatic
            private fun getCandidateConstructors(type: Class<*>): Array<Constructor<*>> {
                if (isInnerClass(type)) {
                    return emptyArray()
                }
                return type.declaredConstructors.filter { !it.isSynthetic }.toTypedArray()
            }

            /**
             * 检查给定类, 是否是一个innerClass
             *
             * @param clazz clazz
             * @return 如果含有"this$0"字段, 并且还是合成的, 那么return true; 否则return false
             */
            @JvmStatic
            private fun isInnerClass(clazz: Class<*>): Boolean {
                try {
                    return clazz.getDeclaredField("this${'$'}0").isSynthetic
                } catch (ex: Exception) {
                    return false
                }
            }

            /**
             * 检查给定的类当中, 是否有[Autowired]注解标注的构造器?
             *
             * @param type type
             * @return 如果存在有Autowired的构造器, return true; 否则return false
             */
            @JvmStatic
            private fun isAutowiredPresent(type: Class<*>): Boolean {
                if (type.declaredConstructors.map(MergedAnnotations::from)
                        .any { it.isPresent(Autowired::class.java) }
                ) {
                    return true
                }
                val userClass = ClassUtils.getUserClass(type)
                return if (userClass === type) false else isAutowiredPresent(userClass)
            }
        }
    }
}