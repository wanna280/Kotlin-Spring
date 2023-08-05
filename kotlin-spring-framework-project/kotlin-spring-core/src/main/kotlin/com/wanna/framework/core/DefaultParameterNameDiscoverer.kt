package com.wanna.framework.core

/**
 * 这是一个默认的参数名的发现器的实现, 它组合了多个参数名发现器, 去进行实现参数名的组合推断
 *
 * * (1)StandardReflectionParameterNameDiscoverer是使用JDK1.8当中提供的标准反射机制去进行实现;
 * * (2)LocalVariableTableParameterNameDiscoverer是使用ASM技术基于ClassReader去读取ClassFile进行局部变量表的访问去进行参数名的发现;
 * * (3)KotlinReflectionParameterNameDiscoverer是基于Kotlin反射的方式去使用Kotlin反射的方式去获取方法的参数名
 * * (4)未来也许会支持的更多的ParameterNameDiscoverer, 也可以通过addParameterNameDiscoverer方法加入到列表当中
 *
 * @see KotlinReflectionParameterNameDiscoverer
 * @see StandardReflectionParameterNameDiscoverer
 * @see LocalVariableTableParameterNameDiscoverer
 * @see addParameterNameDiscoverer
 */
open class DefaultParameterNameDiscoverer : PrioritizedParameterNameDiscoverer() {
    init {
        // 如果启用了Kotlin反射, 那么加入Kotlin反射的参数名发现器(基于Kotlin反射去实现)
        if (KotlinDetector.isKotlinReflectPresent()) {
            super.addParameterNameDiscoverer(KotlinReflectionParameterNameDiscoverer())
        }
        // 添加标准JDK中的反射的参数名发现器
        super.addParameterNameDiscoverer(StandardReflectionParameterNameDiscoverer())
        // 添加基于局部变量表的参数名发现器(基于ASM技术)
        super.addParameterNameDiscoverer(LocalVariableTableParameterNameDiscoverer())
    }
}