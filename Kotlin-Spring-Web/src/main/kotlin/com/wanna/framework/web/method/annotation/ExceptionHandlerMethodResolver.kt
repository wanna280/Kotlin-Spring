package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Method

/**
 * ExceptionHandler的方法的解析器，负责去解析@ExceptionHandler注解标注的方式
 *
 * @param beanType beanType
 */
open class ExceptionHandlerMethodResolver(private val beanType: Class<*>) {
    companion object {

        // ExceptionHandler的Filter
        private val EXCEPTION_HANDLER_METHODS_FILTER: (Method) -> Boolean = {
            it.isAnnotationPresent(ExceptionHandler::class.java)
        }
    }

    // ExceptionHandler的列表，key-ExceptionType，value-Method
    private val mappedMethods = HashMap<Class<out Throwable>, Method>()

    init {
        ReflectionUtils.doWithMethods(beanType, {
            val exceptionHandler = it.getAnnotation(ExceptionHandler::class.java)
            exceptionHandler.value.forEach { klass ->
                mappedMethods[klass.java] = it
            }
        }, EXCEPTION_HANDLER_METHODS_FILTER)
    }

    /**
     * 是否有Exception的Mapping
     *
     * @return 如果找到了ExceptionHandler的方法，return true；否则return false
     */
    open fun hasExceptionMappings(): Boolean = mappedMethods.isNotEmpty()

    /**
     * 根据异常的类型去找到合适的ExceptionHandler方法
     *
     * @param exception 具体产生的异常
     * @return 该异常对应的HandlerMethod(找不到return null)
     */
    open fun resolveMethod(exception: Throwable): Method? {
        return this.mappedMethods[exception::class.java]
    }
}