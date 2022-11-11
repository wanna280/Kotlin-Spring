package com.wanna.framework.instrument.classloading

import com.wanna.framework.util.ClassUtils
import com.wanna.framework.instrument.InstrumentationSavingAgent
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

/**
 * 这是一个基于Java的Instrument的LoadTimeWeaver，它依赖于JVM的Instrument技术去进行实现；
 * 它应该在JVM运行时去指定一个JavaAgent的jar包去进行实现，Spring官方提供了InstrumentationSavingAgent的jar去提供支持；
 * 它需要添加VM Options(-javaagent:path/to/spring-instrument-{version}.jar)去进行使用；
 * 需要通过JavaAgent，获取到JavaAgent当中提供的Instrumentation并完成保存，才能在这个LoadTimeWeaver当中去进行使用；
 *
 * JavaAgent也可以依赖于Intellij IDEA/Eclipse去完成VM Options的配置；
 *
 * 整个加载时编织的流程如下：
 * (1)在启动参数当中添加JavaAgent，在JVM启动时，会自动去回调JavaAgent的premain方法，JavaAgent保存Instrumentation对象到自己的字段当中 <br/>
 * (2)开启加载时编织(创建InstrumentationLoadTimeWeaver对象指定ClassLoader，并通过addTransformer方法往Instrumentation当中添加Transformer) <br/>
 * (3)在加载类时，流程依次为ClassLoader.loadClass-->ClassLoader.defineClass-->Instrumentation回调所有的ClassFileTransformer <br/>
 *
 * <note>**Instrumentation当中添加的ClassFileTransformer，默认情况下会对所有的ClassLoader的defineClass都产生作用，也就是对所有的类的加载都产生影响，
 * 因此在自定义的ClassFileTransformer当中，我们应当(开头)针对指定的ClassLoader去进行产生加载时编织，而不是对所有的ClassLoader都产生影响**</note>
 *
 * @see LoadTimeWeaver
 * @see Instrumentation
 */
open class InstrumentationLoadTimeWeaver(_classLoader: ClassLoader) : LoadTimeWeaver {

    companion object {

        const val AGENT_MARKER = "com.wanna.framework.instrument.InstrumentationSavingAgent"

        /**
         * 判断JavaAgent的标识类是否在依赖当中，需要通过JavaAgent的方式去进行设置
         *
         * @see com.wanna.framework.instrument.InstrumentationSavingAgent
         */
        private val AGENT_CLASS_PRESENT = ClassUtils.isPresent(AGENT_MARKER)

        /**
         * 如果InstrumentationSavingAgent类存在的话，获取到JVM回调给我们的Instrumentation对象
         *
         * @return 如果该类存在的话，return JVM回调的Instrumentation；如果该类不存在的话，return null
         */
        @JvmStatic
        private fun getInstrumentation(): Instrumentation? {
            if (AGENT_CLASS_PRESENT) {
                return InstrumentationAccessor.getInstrumentation()
            }
            return null
        }

        /**
         * 判断当前VM当中是否存在有Instrumentation的依赖？必须得通过JavaAgent导入了InstrumentationSavingAgent这个类时才能return true
         *
         * @return JVM是否给我们回调了Instrumentation？如果回调了，说明使用将JavaAgent，return true；如果没有回调，值为null，return false
         */
        @JvmStatic
        fun isInstrumentationAvailable() = getInstrumentation() != null
    }

    /**
     * 使用静态内部类，因为类加载采用的是懒加载的策略，因此如果这个类没有被加载时，这个类的内部的代码都不会被检测；
     * 因此使用静态内部类可以避免在内部使用到了某些不存在的依赖时，该依赖不存在时被处理到从而抛出LinkageError(NoClassDefFoundError)；
     * 因为这个类的内部使用到了InstrumentationSavingAgent这个依赖是很有可能不存在的！！！
     */
    class InstrumentationAccessor {
        companion object {
            @JvmStatic
            fun getInstrumentation(): Instrumentation? {
                return InstrumentationSavingAgent.getInstrumentation()
            }
        }

    }

    // 无参数构造器，使用默认的ClassLoader去作为Instrument的ClassLoader
    constructor() : this(ClassUtils.getDefaultClassLoader())

    // 保存JavaAgent当中JVM传递给我们的Instrumentation对象，如果不是以JavaAgent方式启动的，值为null
    private val instrumentation: Instrumentation? = getInstrumentation()

    // 转交给Instrument需要使用到的类加载器
    private val classLoader = _classLoader

    // 维护要对ClassFile去进行转换的Transformer列表，<note>对列表当中元素去进行操作时需要加锁</note>
    private val transformers = ArrayList<ClassFileTransformer>()

    /**
     * 添加ClassFileTransformer到JVM回调的Instrumentation对象当中，并自己保存一份Transformer对象
     *
     * @param transformer 要添加的ClassFileTransformer
     * @throws IllegalStateException 如果InstrumentationSavingAgent是被直接以jar包的方式被添加到依赖当中，而不是以JavaAgent的方式去进行启动
     */
    override fun addTransformer(transformer: ClassFileTransformer) {
        // 将目标ClassFileTransformer使用FilteringClassFileTransformer去进行包装
        val actualTransformer = FilteringClassFileTransformer(classLoader, transformer)
        synchronized(this.transformers) {
            if (this.instrumentation == null) {
                throw IllegalStateException("InstrumentationLoadTimeWeaver必须配合JavaAgent的方式启动，并在JavaAgent当中添加InstrumentationSavingAgent的jar包")
            }
            instrumentation.addTransformer(actualTransformer)  // add to Instrumentation
            transformers += actualTransformer
        }
    }

    /**
     * 获取仪器的ClassLoader
     */
    override fun getInstrumentableClassLoader(): ClassLoader {
        return this.classLoader
    }

    override fun getThrowawayClassLoader(): ClassLoader {
        TODO("Not yet implemented")
    }

    /**
     * 这是一个对targetClassLoader和targetClassFileTransformer的包装，最终使用targetTransformer去对真正的ClassFile的转换；
     * 它也支持去对类加载器不匹配的情况去进行过滤，只对目标ClassLoader去进行字节码的转换(transform)
     */
    class FilteringClassFileTransformer(
        private val targetClassLoader: ClassLoader, private val targetTransformer: ClassFileTransformer
    ) : ClassFileTransformer {

        override fun transform(
            loader: ClassLoader?,
            className: String?,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray?
        ): ByteArray? {
            if (loader !== targetClassLoader) {
                return null
            }
            return targetTransformer.transform(
                loader, className, classBeingRedefined, protectionDomain, classfileBuffer
            )
        }
    }
}