package com.wanna.framework.instrument

import java.lang.instrument.Instrumentation

/**
 * 这是提供JavaAgent的Instrumentation对象的保存的一个单例类，通常此依赖将会被加入到JavaAgent参数当中
 */
object InstrumentationSavingAgent {

    /**
     * JVM回调的Instrumentation对象
     */
    @Volatile
    private var instrumentation: Instrumentation? = null

    /**
     * premain，这个函数的签名是JVM所知道的，JVM会自动去进行回调；
     * 一旦回调，我们就可以将Instrumentation对象去保存到本地的static字段当中，方便被使用者所进行获取
     */
    @JvmStatic
    fun premain(agentArgs: String?, inst: Instrumentation?) {
        instrumentation = inst
    }

    @JvmStatic
    fun agentmain(agentArgs: String?, inst: Instrumentation?) {
        instrumentation = inst
    }

    /**
     * 供外部去获取到JVM回调的Instrumentation对象
     */
    @JvmStatic
    fun getInstrumentation(): Instrumentation? {
        return instrumentation
    }
}