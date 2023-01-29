package com.wanna.framework.context.weaving

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.instrument.classloading.InstrumentationLoadTimeWeaver
import com.wanna.framework.instrument.classloading.LoadTimeWeaver
import com.wanna.framework.lang.Nullable
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

/**
 * 完成AspectJ的依赖编制的启动器, 它会被@EnableAspectJWeaving注解所导入, 去开启AspectJ的运行时编织
 *
 * @see com.wanna.framework.instrument.classloading.InstrumentationLoadTimeWeaver
 * @see com.wanna.framework.instrument.classloading.LoadTimeWeaver
 * @see LoadTimeWeaverAware
 */
open class AspectJWeavingEnabler : BeanFactoryPostProcessor, Ordered, BeanClassLoaderAware, LoadTimeWeaverAware {
    companion object {
        /**
         * AspectJ Aop的Xml文件位置常量
         */
        const val ASPECTJ_AOP_XML_RESOURCE = "META-INF/aop.xml"

        /**
         * 开启AspectJ的编织的支持, 往[InstrumentationLoadTimeWeaver]当中添加AspectJ的Transformer
         *
         * @param loadTimeWeaver 要使用的LoadTimeWeaver
         * @param classLoader LoadTimeWeaver要使用的ClassLoader
         */
        @JvmStatic
        fun enableAspectJWeaving(loadTimeWeaver: LoadTimeWeaver?, classLoader: ClassLoader) {
            var weaverToUse = loadTimeWeaver

            // 如果没有指定LoadTimeWeaver, 那么需要按照情况创建默认的LoadTimeAware, classLoader则使用给定的classLoader
            if (weaverToUse == null) {
                if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
                    weaverToUse = InstrumentationLoadTimeWeaver(classLoader)
                } else {
                    throw IllegalStateException("当前VM当中没有LoadTimeWeaver存在")
                }
            }

            // 往LoadTimeWeaver当中添加一个跳过AspectJ的类的ClassFileTransformer, 提供基于JavaAgent方式的AspectJ字节码增强...
            weaverToUse.addTransformer(AspectJClassBypassingClassFileTransformer(ClassPreProcessorAgentAdapter()))
        }
    }

    /**
     * LoadTimeWeaver
     */
    private var loadTimeWeaver: LoadTimeWeaver? = null

    /**
     * beanClassLoader
     */
    private var beanClassLoader: ClassLoader? = null

    /**
     * 注入用于进行BeanClass的加载的[ClassLoader]
     *
     * @param classLoader beanClassLoader
     */
    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    /**
     * 在完成BeanFactoryPostProcessor的后置处理工作时, 去开启AspectJ的加载时编织
     *
     * @param beanFactory BeanFactory
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        enableAspectJWeaving(this.loadTimeWeaver, this.beanClassLoader!!)
    }

    /**
     * 注入[LoadTimeWeaver]
     *
     * @param loadTimeWeaver LoadTimeWeaver
     */
    override fun setLoadTimeWeaver(loadTimeWeaver: LoadTimeWeaver) {
        this.loadTimeWeaver = loadTimeWeaver
    }

    /**
     * 优先级
     *
     * @return 最高优先级
     */
    override fun getOrder(): Int = Ordered.ORDER_HIGHEST

    /**
     * 对AspectJ的Transformer再进行包装一层, 避免因为依赖当中没有AspectJ的依赖而产生潜在的AspectJ的LinkageError(NoClassDefFoundError)
     *
     * @param delegate Delegate Transformer
     */
    private class AspectJClassBypassingClassFileTransformer(private val delegate: ClassFileTransformer) :
        ClassFileTransformer {
        override fun transform(
            @Nullable loader: ClassLoader?,
            className: String,
            @Nullable classBeingRedefined: Class<*>?,
            @Nullable protectionDomain: ProtectionDomain?,
            @Nullable classfileBuffer: ByteArray?
        ): ByteArray? {
            // 如果AspectJ相关的类(org.aspectj.*), 那么直接去进行跳过处理, pass掉...
            if (className.startsWith("org.aspectj") || className.startsWith("org/aspectj")) {
                return classfileBuffer
            }
            // 如果类名不是AspectJ相关的类, 那么直接使用AspectJ的Transformer作为委托去进行transform
            return delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
        }
    }
}