package com.wanna.framework.context.weaving

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.instrument.classloading.InstrumentationLoadTimeWeaver
import com.wanna.framework.instrument.classloading.LoadTimeWeaver
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

/**
 * 完成AspectJ的依赖编制的启动器
 *
 * @see com.wanna.framework.instrument.classloading.InstrumentationLoadTimeWeaver
 * @see com.wanna.framework.instrument.classloading.LoadTimeWeaver
 * @see LoadTimeWeaverAware
 */
open class AspectJWeavingEnabler : BeanFactoryPostProcessor, Ordered, BeanClassLoaderAware, LoadTimeWeaverAware {

    // LoadTimeWeaver
    private var loadTimeWeaver: LoadTimeWeaver? = null

    // beanClassLoader
    private var beanClassLoader: ClassLoader? = null

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    /**
     * 在完成BeanFactoryPostProcessor的后置处理工作时，去开启AspectJ的加载时编织
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        enableAspectJWeaving(this.loadTimeWeaver, this.beanClassLoader)
    }

    override fun setLoadTimeWeaver(loadTimeWeaver: LoadTimeWeaver) {
        this.loadTimeWeaver = loadTimeWeaver
    }

    override fun getOrder(): Int {
        return Ordered.ORDER_HIGHEST
    }

    companion object {

        // AspectJ Aop的Xml文件位置常量
        const val ASPECTJ_AOP_XML_RESOURCE = "META-INF/aop.xml"

        /**
         * 开启AspectJ的编织的支持，往InstrumentationLoadTimeWeaver当中添加AspectJ的Transformer
         */
        @JvmStatic
        fun enableAspectJWeaving(loadTimeWeaver: LoadTimeWeaver?, classLoader: ClassLoader?) {
            var weaverToUse = loadTimeWeaver
            if (weaverToUse == null) {
                if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
                    weaverToUse = InstrumentationLoadTimeWeaver(classLoader!!)
                } else {
                    throw IllegalStateException("VM当中没有LoadTimeWeaver存在")
                }
            }

            // 往LoadTimeWeaver当中添加ClassFileTransformer
            weaverToUse.addTransformer(AspectJClassBypassingClassFileTransformer(ClassPreProcessorAgentAdapter()))
        }
    }

    /**
     * 对类再进行包装一层，避免因为依赖当中没有AspectJ的依赖而产生潜在的AspectJ的LinkageError(NoClassDefError)
     */
    private class AspectJClassBypassingClassFileTransformer(private val delegate: ClassFileTransformer) :
        ClassFileTransformer {
        override fun transform(
            loader: ClassLoader?,
            className: String,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray?
        ): ByteArray? {
            // 如果类名是AspectJ相关的jar包，那么直接去进行跳过处理
            if (className.startsWith("org.aspectj") || className.startsWith("org/aspectj")) {
                return classfileBuffer
            }
            return delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
        }
    }
}