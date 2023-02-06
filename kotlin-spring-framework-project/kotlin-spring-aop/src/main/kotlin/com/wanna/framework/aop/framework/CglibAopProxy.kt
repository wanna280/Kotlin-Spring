package com.wanna.framework.aop.framework

import com.wanna.framework.aop.ReflectiveMethodInvocation
import com.wanna.framework.core.cglib.core.SpringNamingPolicy
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 基于Cglib的动态代理, 需要使用基于ASM技术去实现的Enhancer去进行创建代理
 *
 * @param config AdvisedSupport, 维护了代理需要用到的相关的各个组件
 */
open class CglibAopProxy(private val config: AdvisedSupport) : AopProxy {
    override fun getProxy(): Any {
        return getProxy(ClassUtils.getDefaultClassLoader())
    }

    override fun getProxy(classLoader: ClassLoader?): Any {
        val enhancer = createEnhancer()

        val rootClass = this.config.getTargetClass() ?: throw IllegalStateException("CGLIB代理当中必须设置targetClass")
        var proxySuperClass = rootClass

        // 如果含有'$$', 说明已经被CGLIB代理过, 需要使用它的父类去生成代理
        if (rootClass.name.contains("$$")) {
            proxySuperClass = rootClass.superclass
            val additionalInterfaces = rootClass.interfaces
            additionalInterfaces.forEach { this.config.addInterface(it) }
        }

        if (classLoader != null) {
            enhancer.classLoader = classLoader  // set ClassLoader
        }
        enhancer.namingPolicy = SpringNamingPolicy  // set NamingPolicy
        enhancer.setCallback(DynamicAdvisedInterceptor(this.config))
        enhancer.setSuperclass(proxySuperClass)
        enhancer.setInterfaces(config.getInterfaces().toTypedArray())

        // create proxy Instance
        return createProxyClassAndInstance(enhancer)
    }

    protected open fun createProxyClassAndInstance(enhancer: Enhancer): Any {
        return enhancer.create()
    }

    protected open fun createEnhancer(): Enhancer {
        return Enhancer()
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
        proxy: Any,
        target: Any?,
        method: Method,
        args: Array<Any?>?,
        targetClass: Class<*>?,
        interceptorsAndDynamicMethodMatchers: List<Any>,
        methodProxy: MethodProxy
    ) : ReflectiveMethodInvocation(
        proxy, target, method, args, targetClass, interceptorsAndDynamicMethodMatchers
    ) {
        private var methodProxy: MethodProxy? = null

        init {

            // 如果是不是Object类的方法才使用methodProxy去进行执行
            this.methodProxy =
                if (Modifier.isPublic(method.modifiers) && !ReflectionUtils.isObjectMethod(method)) methodProxy else null
        }

        override fun invokeJoinpoint(): Any? {
            return if (methodProxy != null) {
                methodProxy!!.invoke(this.getTarget(), this.getArguments())
            } else {
                super.invokeJoinpoint()
            }
        }
    }
}