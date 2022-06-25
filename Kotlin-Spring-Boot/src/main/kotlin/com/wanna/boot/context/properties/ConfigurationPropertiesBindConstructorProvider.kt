package com.wanna.boot.context.properties

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.Constructor

/**
 * 它主要是解析@ConfigurationProperties注解的Bean当中的@ConstructorBinding注解，去判断一个ConfigurationPropertiesBean
 * 应该使用构造器的方式去进行绑定？还是应该使用JavaBean的setter的方式去进行绑定
 *
 * @see ConfigurationPropertiesBean
 */
class ConfigurationPropertiesBindConstructorProvider {

    companion object {
        @JvmField
        val INSTANCE = ConfigurationPropertiesBindConstructorProvider()  // singleton object for visit
    }


    /**
     * @return 获取@ConstructorBinding标注的的构造器；找不到的话，return null
     */
    fun getBindConstructor(type: Class<*>): Constructor<*>? {

        // 1.首先去寻找标注了@ConstructorBinding的构造器
        val constructor = findConstructorBindingAnnotatedConstructor(type)

        // 2.如果没有找到合适的@ConstructorBinding注解的构造器，但是在类上标注了@ConstructorBinding注解，那么去匹配一个合适的构造器
        if (constructor == null || isConstructorBindingAnnotatedType(type)) {
            return deduceBindConstructor(type)
        }
        return constructor
    }

    /**
     * 找到@ConstructorBinding注解的构造器
     */
    private fun findConstructorBindingAnnotatedConstructor(type: Class<*>): Constructor<*>? {
        // TODO Kotlin的主构造器的推断
        return findAnnotatedConstructor(type, type.declaredConstructors)
    }

    /**
     * 从指定的构造器列表当中去找到标注的@ConstructorBinding注解的构造器
     *
     * @param type 类型
     * @param candidates 要去检查@ConstructorBinding注解的候选的构造器列表
     * @return 如果找到了，那么return 匹配的构造器；不然return null
     * @throws IllegalStateException 注解标注在无参数构造器上/找到了多个标注了注解的构造器
     */
    private fun findAnnotatedConstructor(type: Class<*>, candidates: Array<Constructor<*>>): Constructor<*>? {
        var constructor: Constructor<*>? = null
        candidates.forEach {
            if (AnnotatedElementUtils.isAnnotated(it, ConstructorBinding::class.java)) {
                if (it.parameterCount == 0) {
                    throw IllegalStateException("@ConstructorBinding注解不能标注在[type=$type]的无参数构造器上")
                }
                if (constructor != null) {
                    throw IllegalStateException("@ConstructorBinding注解只能标注在[type=$type]其中一个有参数构造器上")
                }
                constructor = it
            }
        }
        return constructor
    }

    /**
     * 判断类上是否标注了@ConstructorBinding注解
     *
     * @return 标注了return true；不然return false
     */
    private fun isConstructorBindingAnnotatedType(type: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(type, ConstructorBinding::class.java)
    }

    /**
     * 推断合适的构造器作为@ConstructorBinding的构造器
     */
    private fun deduceBindConstructor(type: Class<*>): Constructor<*>? {
        // TODO Kotlin的类型匹配

        // 如果是个Java类，并且只要一个有参数构造器的话，return；不然return null
        if (type.declaredConstructors.size == 1 && type.declaredConstructors[0].parameterCount > 0) {
            return type.declaredConstructors[0]
        }
        return null
    }

}