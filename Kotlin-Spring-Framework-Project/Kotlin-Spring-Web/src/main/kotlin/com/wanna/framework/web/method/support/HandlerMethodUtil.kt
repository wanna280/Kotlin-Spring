package com.wanna.framework.web.method.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.web.method.HandlerMethod
import java.lang.reflect.Method

/**
 * 这是一个HandlerMethod的工具类，负责去完成Handler对象的创建工作；
 * 因为Kotlin当中所有的构造器必须要调用主构造器，这一点对于多个构造器要使用不同类型的参数来说，会存在有很多不方便，因此我们提供了这样的一个工具类；
 * 方便去进行HandlerMethod的构建，目的是为了做到类似提供多个构造器的方式，去进行HandlerMethod以及它的子类的构建
 *
 * @see HandlerMethod
 * @see InvocableHandlerMethod
 */
object HandlerMethodUtil {
    /**
     * 根据HandlerMethod去重新构建一个Handler，目的是将Handler从beanName替换为Bean
     *
     * @param handlerMethod 之前的HandlerMethod
     * @param handler 要替换的beanObject
     * @param type 要创建的HandlerMethod的类型，可以为HandlerMethod的子类
     * @return 创建好的HandlerMethod
     * @param T 要创建的HandlerMethod的类型
     */
    @JvmStatic
    fun <T : HandlerMethod> newHandlerMethod(handlerMethod: HandlerMethod, handler: Any, type: Class<T>): T {
        val hm = newHandlerMethod(handlerMethod, type)
        hm.bean = handler
        return hm
    }

    /**
     * 根据拷贝一份HandlerMethod，将HandlerMethod当中的相关的数据都拷贝到新的HandlerMethod对象当中
     *
     * @param handlerMethod 之前的HandlerMethod
     * @param type 要创建的HandlerMethod的类型，可以为HandlerMethod的子类
     * @return 创建好的HandlerMethod
     * @param T 要创建的HandlerMethod的类型
     */
    @JvmStatic
    fun <T : HandlerMethod> newHandlerMethod(handlerMethod: HandlerMethod, type: Class<T>): T {
        val hm = ClassUtils.newInstance(type)
        hm.method = handlerMethod.method
        hm.parameters = handlerMethod.parameters
        hm.beanType = handlerMethod.beanType
        hm.beanFactory = handlerMethod.beanFactory
        hm.handlerMethod = handlerMethod
        hm.bean = handlerMethod.bean
        return hm
    }

    /**
     * 根据beanFactory、beanName、method去构建一个HandlerMethod，后续可以通过beanFactory.getBean(beanName)去进行获取；
     * 但是这里并不直接设置为Bean，因为HandlerMethod是需要加入到缓存当中的，后续需要从缓存当中获取，但是有可能Bean是会在中途当中发生替换的；
     * 比如@RefreshScope注解自定义了Bean的作用域为RefreshScope，当环境信息发生改编时，需要重新去createBean并加入到RefreshScope作用域当中；
     * 而不是使用之前创建好的beanObject，因此作为缓存时，需要使用的必须是基于beanName方式的HandlerMethod，不能使用基于beanObject方式的HandlerMethod
     *
     * @param beanFactory beanFactory
     * @param beanName beanName
     * @return 构建好的HandlerMethod
     * @param T 要创建的HandlerMethod的类型
     */
    @JvmStatic
    fun <T : HandlerMethod> newHandlerMethod(
        beanFactory: BeanFactory,
        beanName: String,
        method: Method,
        type: Class<T>
    ): T {
        val hm = ClassUtils.newInstance(type)
        hm.bean = beanName
        hm.beanFactory = beanFactory
        hm.method = method
        hm.parameters = Array(method.parameterCount) { MethodParameter(method, it) }
        hm.beanType = beanFactory.getType(beanName)
        return hm
    }

    /**
     * 使用Bean和Method，直接去构建HandlerMethod，因为不用beanName，因此不需要使用BeanFactory；
     * 从缓存当中获取到的是一个基于beanName和beanFactory的HandlerMethod；真正需要执行时，需要的是bean，因此需要一次转换；
     * 这个方法就可以为实现这次转换提供支持，去创建一个新的HandlerMethod
     *
     * @param bean beanObject
     * @param method 处理请求的方法
     * @param type 要创建的HandlerMethod的类型，可以为HandlerMethod的子类
     * @return 创建好的HandlerMethod
     * @param T 要创建的HandlerMethod的类型
     */
    @JvmStatic
    fun <T : HandlerMethod> newHandlerMethod(bean: Any, method: Method, type: Class<T>): T {
        val hm = ClassUtils.newInstance(type)
        hm.bean = bean
        hm.parameters = Array(method.parameterCount) { MethodParameter(method, it) }
        hm.beanType = bean::class.java
        hm.method = method
        return hm
    }
}