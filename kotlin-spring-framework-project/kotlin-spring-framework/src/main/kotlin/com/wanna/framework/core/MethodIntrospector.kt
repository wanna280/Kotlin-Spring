package com.wanna.framework.core

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.ReflectionUtils.MethodMatcher
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/13
 */
object MethodIntrospector {

    /**
     * 从给定的Class的所有方法当中, 去找到合适的要去进行寻找的方法列表
     *
     * @param targetType 要去进行寻找方法的目标类
     * @param metadataLookup 对于什么情况下该方法需要收集起来? 如果return null代表该方法不符合要求
     * @return 选出来的所有的合适的方法(Key-Method, Value-MetadataLookup的转换结果)
     */
    @JvmStatic
    fun <T> selectMethods(targetType: Class<*>, metadataLookup: MetadataLookup<T>): Map<Method, T> {
        val methodMap = LinkedHashMap<Method, T>()
        val handlerTypes = LinkedHashSet<Class<*>>()
        if (!Proxy.isProxyClass(targetType)) {
            handlerTypes += ClassUtils.getUserClass(targetType)
        }
        handlerTypes += ClassUtils.getAllInterfacesForClassAsSet(targetType)

        // 从给定的类, 以及它的所有的接口当中, 去寻找到符合MetadataLookup的要求的方法
        for (handlerType in handlerTypes) {
            ReflectionUtils.doWithMethods(handlerType, {
                val result = metadataLookup.inspect(it)
                if (result != null) {
                    methodMap[it] = result
                }
            }, ReflectionUtils.USER_DECLARED_METHODS)

        }
        return methodMap
    }

    /**
     * 选取出来目标类上的所有方法当中, 符合给定的[MethodMatcher]的要求的那些方法
     *
     * @param targetType targetType
     * @param methodMatcher 进行匹配的目标方法
     * @return targetType的所有方法当中符合MethodMatcher的方法列表
     */
    @JvmStatic
    fun selectMethods(targetType: Class<*>, methodMatcher: MethodMatcher): Set<Method> {
        return selectMethods(targetType, MetadataLookup<Any?> {
            if (methodMatcher.matches(it)) true else null
        }).keys.toSet()
    }


    fun interface MetadataLookup<T> {

        @Nullable
        fun inspect(method: Method): T?
    }
}