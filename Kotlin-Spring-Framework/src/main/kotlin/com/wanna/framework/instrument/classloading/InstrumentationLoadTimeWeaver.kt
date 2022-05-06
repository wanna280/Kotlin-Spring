package com.wanna.framework.instrument.classloading

import com.wanna.framework.core.util.ClassUtils
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
 * JavaAgent也可以依赖于Intellij IDEA/Eclipse去完成VM Options的配置
 *
 * @see LoadTimeWeaver
 * @see Instrumentation
 */
open class InstrumentationLoadTimeWeaver(_classLoader: ClassLoader) : LoadTimeWeaver {

    companion object {
        /**
         * 判断JavaAgent的标识类是否在依赖当中，需要通过JavaAgent的方式去进行设置
         */
        private val AGENT_CLASS_PRESENT = ClassUtils.isPresent(
            "com.wanna.framework.instrument.InstrumentationSavingAgent",
            InstrumentationLoadTimeWeaver::class.java.classLoader
        )

        /**
         * 如果Agent类存在的话，获取到JVM回调给我们的Instrumentation对象
         */
        @JvmStatic
        private fun getInstrumentation(): Instrumentation? {
            if (AGENT_CLASS_PRESENT) {
                return InstrumentationAccessor.getInstrumentation()
            }
            return null
        }

        /**
         * 判断当前VM当中是否存在有Instrumentation的依赖？
         */
        @JvmStatic
        fun isInstrumentationAvailable() = getInstrumentation() != null
    }

    /**
     * 使用静态内部类，懒加载的方式，避免该依赖不存在时被处理到从而抛出LinkageError(NoClassDefError)
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

    // 保存JavaAgent当中JVM传递给我们的Instrumentation对象
    private val instrumentation: Instrumentation? = getInstrumentation()

    // 转交给Instrument需要使用到的类加载器
    private val classLoader = _classLoader

    // 维护要对ClassFile去进行转换的Transformer
    private val transformers = ArrayList<ClassFileTransformer>()

    /**
     * 添加ClassFileTransformer到JVM回调的Instrumentation对象当中
     */
    override fun addTransformer(transformer: ClassFileTransformer) {
        // 将目标ClassFileTransformer使用FilteringClassFileTransformer去进行包装
        val actualTransformer = FilteringClassFileTransformer(classLoader, transformer)
        synchronized(this.transformers) {
            instrumentation!!.addTransformer(actualTransformer)
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
     * 它也支持去对类加载器不匹配的情况去进行过滤
     */
    class FilteringClassFileTransformer(
        private val targetClassLoader: ClassLoader,
        private val targetTransformer: ClassFileTransformer
    ) : ClassFileTransformer {

        override fun transform(
            loader: ClassLoader?,
            className: String?,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray?
        ): ByteArray? {
            if (loader != targetClassLoader) {
                return null
            }
            return targetTransformer.transform(
                loader, className, classBeingRedefined, protectionDomain, classfileBuffer
            )
        }
    }
}