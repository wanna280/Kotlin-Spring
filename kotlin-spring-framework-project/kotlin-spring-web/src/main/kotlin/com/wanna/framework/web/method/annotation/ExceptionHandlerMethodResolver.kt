package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodIntrospector
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.bind.annotation.ExceptionHandler
import java.lang.reflect.Method

/**
 * ExceptionHandler的方法的解析器, 负责去解析@ExceptionHandler注解标注的方式;
 * 它会维护一个类上面的所有的@ExceptionHandler的映射关系, key-exceptionType, value-Method
 *
 * @param beanType beanType(要去匹配@ExceptionHandler的类)
 */
open class ExceptionHandlerMethodResolver(private val beanType: Class<*>) {
    companion object {

        /**
         * 对于@ExceptionHandler方法的寻找的Filter
         */
        @JvmStatic
        private val EXCEPTION_HANDLER_METHODS_FILTER = ReflectionUtils.MethodMatcher {
            AnnotatedElementUtils.hasAnnotation(it, ExceptionHandler::class.java)
        }
    }

    // ExceptionHandler的列表, key-ExceptionType, value-Method
    private val mappedMethods = HashMap<Class<out Throwable>, Method>()


    init {
        // 获取该类上的全部@ExceptionHandler方法, 去完成Mapping的注册
        val selectMethods = MethodIntrospector.selectMethods(beanType, EXCEPTION_HANDLER_METHODS_FILTER)
        for (method in selectMethods) {
            val exceptionMappings = detectExceptionMappings(method)
            for (exceptionMapping in exceptionMappings) {
                addExceptionMapping(method, exceptionMapping)
            }
        }
    }

    /**
     * 将给定的ExceptionType和方法之间的映射关系建立起来
     *
     * @param method Method
     * @param exceptionType ExceptionType
     */
    private fun addExceptionMapping(method: Method, exceptionType: Class<out Throwable>) {
        this.mappedMethods[exceptionType] = method
    }

    /**
     * 从给定的类上去探测[ExceptionHandler]注解当中配置的支持去处理的异常类型
     *
     * @param method 要去进行探测的目标方法
     * @return 探测到的要去进行处理的异常类型
     */
    private fun detectExceptionMappings(method: Method): List<Class<out Throwable>> {
        val exceptionHandler =
            AnnotatedElementUtils.getMergedAnnotation(method, ExceptionHandler::class.java) ?: return emptyList()
        return exceptionHandler.value.map { it.java }.toList()
    }

    /**
     * 是否有Exception的Mapping, 如果该类上有@ExceptionHandler的方法, 则有Mapping
     *
     * @return 如果找到了ExceptionHandler的方法, return true; 否则return false
     */
    open fun hasExceptionMappings(): Boolean = mappedMethods.isNotEmpty()

    /**
     * 根据异常的类型去找到合适的ExceptionHandler方法
     *
     * @param exception 具体产生的异常
     * @return 该异常对应的HandlerMethod(找不到return null)
     */
    @Nullable
    open fun resolveMethod(exception: Throwable): Method? {
        return this.mappedMethods[exception::class.java]
    }
}