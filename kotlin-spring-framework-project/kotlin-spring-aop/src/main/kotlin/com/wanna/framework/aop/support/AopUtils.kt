package com.wanna.framework.aop.support

import com.wanna.framework.aop.*
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Spring Aop的相关工具类
 */
object AopUtils {

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
    @JvmStatic
    fun invokeJoinpointUsingReflection(target: Any?, method: Method, args: Array<Any?>): Any? {
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