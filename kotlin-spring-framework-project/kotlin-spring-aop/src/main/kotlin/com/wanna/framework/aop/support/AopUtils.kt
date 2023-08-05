package com.wanna.framework.aop.support

import com.wanna.framework.aop.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Spring Aop的相关工具类
 */
object AopUtils {

    /**
     * 检查给定的实例, 是否是一个被SpringProxy所代理的实例?
     *
     * @param instance 要去进行检查的实例
     * @return 如果它是Spring代理对象, 那么return true; 否则return false
     */
    @JvmStatic
    fun isAopProxy(@Nullable instance: Any?): Boolean {
        return instance is SpringProxy
                && (Proxy.isProxyClass(instance.javaClass) || instance.javaClass.name.contains(ClassUtils.CGLIB_CLASS_SEPARATOR))
    }

    /**
     * 检查给定的实例, 是否是一个被JDK动态代理产生的类?
     *
     * @param instance 要去进行检查的实例对象
     * @return 如果它是JDK动态代理产生的对象, return true; 否则return false
     */
    @JvmStatic
    fun isJdkDynamicProxy(@Nullable instance: Any?): Boolean {
        return instance is SpringProxy && Proxy.isProxyClass(instance.javaClass)
    }

    /**
     * 检查给定的实例, 是否是一个被JDK动态代理产生的类?
     *
     * @param instance 要去进行检查的实例对象
     * @return 如果它是CGLIB动态代理产生的对象, return true; 否则return false
     */
    @JvmStatic
    fun isCglibProxy(@Nullable instance: Any?): Boolean {
        return instance is SpringProxy && instance.javaClass.name.contains(ClassUtils.CGLIB_CLASS_SEPARATOR)
    }

    /**
     * 获取给定的实例对象的原始对象类型(如果是CGLIB代理, 那么返回superClass)
     *
     * @param candidate 要去获取原始类型的对象
     * @return 获取到的给定的对象的原始类型
     */
    @JvmStatic
    fun getTargetClass(candidate: Any): Class<*> {
        var targetClass: Class<*>? = null

        // 如果是TargetClassAware, 那么优先从这里去进行获取
        if (candidate is TargetClassAware) {
            targetClass = candidate.getsTargetClass()
        }
        if (targetClass == null) {
            targetClass = if (isCglibProxy(candidate)) candidate.javaClass.superclass else candidate.javaClass
        }
        return targetClass!!
    }

    /**
     * 寻找到所有的可以去apply给当前的beanClass的Advisor列表
     *
     * @param advisors 候选的Advisor列表
     * @param targetClass 目标类
     * @return 匹配到的可以应用给targetClass的Advisor列表
     */
    @JvmStatic
    fun findAdvisorsThatCanApply(advisors: List<Advisor>, targetClass: Class<*>): List<Advisor> {
        val result = ArrayList<Advisor>()
        advisors.forEach {
            // 使用ClassFilter和MethodMatcher去进行匹配
            if (canApply(it, targetClass)) {
                result += it
            }
        }
        return result
    }

    /**
     * 指定的Advisor是否可以应用给目标类?
     *
     * @param advisor 要匹配的Advisor
     * @param targetClass 目标类
     * @return 该Advisor是否可以应用给targetClass? 可以应用return true; 否则return false
     */
    @JvmStatic
    fun canApply(advisor: Advisor, targetClass: Class<*>): Boolean {
        // 如果它是一个IntroductionAdvisor, 获取ClassFilter去进行匹配
        if (advisor is IntroductionAdvisor) {
            return advisor.getClassFilter().matches(targetClass)
        }
        // 如果它是一个PointcutAdvisor, 那么使用ClassFilter/MethodMatcher去进行匹配
        if (advisor is PointcutAdvisor) {
            return canApply(advisor.getPointcut(), targetClass)
        }
        return true
    }

    /**
     * 判断某个Pointcut是否可以应用给目标类
     *
     * @param pointcut 要去进行匹配的pointcut
     * @param targetClass 目标类
     */
    @JvmStatic
    fun canApply(pointcut: Pointcut, targetClass: Class<*>): Boolean {
        // 如果ClassFilter去进行匹配, 如果不匹配的话, 直接return false
        if (!pointcut.getClassFilter().matches(targetClass)) {
            return false
        }
        // 如果ClassFilter匹配的话, 那么使用MethodMatcher去进行匹配...只要匹配到其中一个方法就return true
        val methodMatcher = pointcut.getMethodMatcher()
        if (methodMatcher == MethodMatcher.TRUE) {
            return true
        }
        // 获取它的所有的接口
        val classes = ArrayList<Class<*>>()
        classes += targetClass
        classes += ClassUtils.getAllInterfacesForClassAsSet(targetClass)

        // 遍历所有接口的所有方法, 去进行匹配, 如果匹配了, 那么return true
        classes.forEach { clazz ->
            val declaredMethods = ReflectionUtils.getDeclaredMethods(clazz)
            declaredMethods.forEach { method ->
                if (methodMatcher.matches(method, clazz)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 使用反射去执行目标Joinpoint
     *
     * @param target 目标对象
     * @param method 目标方法
     * @param args 方法参数
     */
    @Nullable
    @JvmStatic
    fun invokeJoinpointUsingReflection(@Nullable target: Any?, method: Method, args: Array<Any?>): Any? {
        try {
            ReflectionUtils.makeAccessible(method)
            return ReflectionUtils.invokeMethod(method, target, *args)
        } catch (ex: InvocationTargetException) {
            throw ex.targetException
        } catch (ex: IllegalArgumentException) {
            throw ex
        }
    }
}