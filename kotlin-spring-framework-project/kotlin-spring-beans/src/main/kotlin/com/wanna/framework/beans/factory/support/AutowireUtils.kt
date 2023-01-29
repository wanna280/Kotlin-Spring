package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.ObjectFactory
import java.io.Serializable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Autowire的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
object AutowireUtils {

    /**
     * 解析用来进行Autowire的值
     *
     * @param autowireValue 原始的需要去进行注入的值
     * @param requiredType 需要进行注入的值的类型
     * @return 转换得到的要去进行注入的值
     */
    @JvmStatic
    fun resolveAutowiringValue(autowireValue: Any, requiredType: Class<*>): Any {

        // 如果autowireValue是一个ObjectFactory, 但是想要的又不是一个ObjectFactory
        // 那么我们需要使用ObjectFactory.getObject去转换成为目标类型去进行注入
        if (autowireValue is ObjectFactory<*> && !requiredType.isInstance(autowireValue)) {

            // 如果autowireValue是Serializable的, 但是requiredType是一个接口的话
            // 那么我们需要使用JDK动态代理去生成一个代理对象去委托一层...
            if (autowireValue is Serializable && requiredType.isInterface) {
                return Proxy.newProxyInstance(
                    requiredType.classLoader,
                    arrayOf(requiredType),
                    ObjectFactoryDelegatingInvocationHandler(autowireValue)
                )
            }

            // 如果是比较普通的ObjectFactory的话, 那么我们直接ObjectFactory.getObject
            return autowireValue.getObject()!!
        }
        return autowireValue
    }

    private class ObjectFactoryDelegatingInvocationHandler(private val objectFactory: ObjectFactory<*>) :
        InvocationHandler, Serializable {
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
            when (method.name) {
                "equals" ->
                    return proxy === args[0]
                "hashCode" ->
                    return System.identityHashCode(proxy)
                "toString" -> return objectFactory.toString()
            }
            return try {
                method.invoke(objectFactory.getObject(), args)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
        }
    }
}