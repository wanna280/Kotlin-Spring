package com.wanna.framework.instrument

import java.lang.instrument.Instrumentation
import javax.annotation.Nullable

/**
 * 这是提供JavaAgent的[Instrumentation]对象的保存的一个单例类, 通常此依赖将会被加入到JavaAgent参数当中;
 * 同时支持premain和agentmain两种方式去获取[Instrumentation]对象, 使用时需要添加下面这样的VM参数(VM Option)
 * "-javaagent:/path/to/kotlin-spring-instrument.jar"
 *
 * @see Instrumentation
 */
@Suppress("UNUSED")
object InstrumentationSavingAgent {
    /**
     * JVM回调的[Instrumentation]对象, 我们需要去进行保存, 方便把它交给应用程序, 去进行类的各种转换操作
     */
    @Nullable
    @Volatile
    private var instrumentation: Instrumentation? = null

    /**
     * premain, 这个函数的签名是JVM所知道的, JVM会自动去进行回调, 来自于JDK1.5;
     * 这种方式需要在应用当中添加VM Option的方式启动("-javaagent:/path/to/agent.jar");
     * 一旦JVM进行回调, 我们就可以将Instrumentation对象去保存到本地的static字段当中, 方便被使用者所进行获取;
     *
     * @param inst JVM去进行自动自动回调的[Instrumentation]
     */
    @JvmStatic
    fun premain(@Nullable agentArgs: String?, inst: Instrumentation) {
        instrumentation = inst
    }

    /**
     * agentmain, 这个函数的签名是JVM所知道的, JVM会自动去进行回调, 来自于JDK1.6新增的attach
     * 使用agentmain这种方式可以在应用启动之后, 使用外部应用程序去进行attach的方式进行启动;
     * 比如Alibaba开源Arthas监控软件, 就是使用的agentmain后期attach去进行实现;
     * JavaAgent的attach方法的使用示例代码(Kotlin)如下：
     * ```kotlin
     *   val pid = "remoteProcessPid"
     *   val pathToAgent = "/path/to/agent.jar"
     *   val virtualMachine = VirtualMachine.attach(pid)
     *   try {
     *     virtualMachine.loadAgent(pathToAgent)
     *   } finally {
     *     virtualMachine.detach()
     *   }
     * ```
     *
     * @param inst JVM自动回调的[Instrumentation]
     */
    @JvmStatic
    fun agentmain(@Nullable agentArgs: String?, inst: Instrumentation) {
        instrumentation = inst
    }

    /**
     * 供外部应用程序去获取到JVM自动回调的[Instrumentation]对象
     *
     * @return 如果JVM已经回调了[Instrumentation]对象, return Instrumentation; 如果JVM没有回调, 那么return null
     */
    @Nullable
    @JvmStatic
    fun getInstrumentation(): Instrumentation? = instrumentation
}