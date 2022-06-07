package com.wanna.framework.aop.framework

import com.wanna.framework.aop.ReflectiveMethodInvocation
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 基于Cglib的动态代理，需要使用基于ASM技术去实现的Enhancer去进行创建代理
 */
open class CglibAopProxy(private val config: AdvisedSupport) : AopProxy {
    override fun getProxy(): Any {
        return getProxy(ClassUtils.getDefaultClassLoader())
    }

    override fun getProxy(classLoader: ClassLoader?): Any {
        val enhancer = Enhancer()
        enhancer.setCallback(DynamicAdvisedInterceptor(this.config))
        enhancer.setSuperclass(config.getTargetClass())
        enhancer.setInterfaces(config.getInterfaces().toTypedArray())
        return enhancer.create()
    }

    private class DynamicAdvisedInterceptor(private val advised: AdvisedSupport) : MethodInterceptor {
        override fun intercept(proxy: Any, method: Method, args: Array<Any?>?, methodProxy: MethodProxy): Any? {
            val targetSource = advised.getTargetSource()
            val target = targetSource.getTarget()
            val targetClass = targetSource.getTargetClass()

            // 获取MethodInterceptor链
            val chain = advised.getInterceptorsAndDynamicInterceptionAdvice(method, method.declaringClass)
            return CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed()
        }
    }

    private class CglibMethodInvocation(
        _proxy: Any,
        _target: Any?,
        _method: Method,
        _args: Array<Any?>?,
        _targetClass: Class<*>?,
        _interceptorsAndDynamicMethodMatchers: List<Any>,
        _methodProxy: MethodProxy
    ) : ReflectiveMethodInvocation(
        _proxy, _target, _method, _args, _targetClass, _interceptorsAndDynamicMethodMatchers
    ) {
        private var methodProxy: MethodProxy? = null

        init {

            // 如果是不是Object类的方法才使用methodProxy去进行执行
            methodProxy =
                if (Modifier.isPublic(_method.modifiers) && !ReflectionUtils.isObjectMethod(_method)) _methodProxy else null
        }

        override fun invokeJoinpoint(): Any? {
            if (methodProxy != null) {
                return methodProxy!!.invoke(this.getTarget(), this.getArguments())
            } else {
                return super.invokeJoinpoint()
            }
        }
    }
}