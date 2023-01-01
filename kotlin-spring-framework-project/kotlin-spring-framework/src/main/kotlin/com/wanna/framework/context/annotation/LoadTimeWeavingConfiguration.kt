package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.EnableAspectJWeaving.*
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.weaving.AspectJWeavingEnabler
import com.wanna.framework.context.weaving.DefaultContextLoadTimeWeaver
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是一个用于开启AspectJ的运行时编制的配置类，负责给容器当中注册用于完成LoadTimeWeaving的相关组件
 *
 * @see LoadTimeWeavingConfigurer
 * @see EnableAspectJWeaving
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
open class LoadTimeWeavingConfiguration : BeanClassLoaderAware, ImportAware {

    // LoadTimeWeaverConfigurer，可以自定义LoadTimeWeaver
    private var loadTimeWeaverConfigurer: LoadTimeWeavingConfigurer? = null

    // beanClassLoader
    private var classLoader: ClassLoader? = null

    // @Import AnnotationMetadata
    private var annotationMetadata: AnnotationMetadata? = null

    /**
     * 如果必要的话，在这里去注入LoadTimeWavingConfigurer，如果找不到的话就pass
     */
    @Autowired(required = false)
    fun setLoadTimeWeavingConfigurer(loadTimeWeavingConfigurer: LoadTimeWeavingConfigurer?) {
        this.loadTimeWeaverConfigurer = loadTimeWeavingConfigurer
    }

    override fun setImportMetadata(annotationMetadata: AnnotationMetadata) {
        this.annotationMetadata = annotationMetadata
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    /**
     * 给容器中导入一个InstrumentationLoadTimeWeaver组件，去支持AspectJ的编织
     *
     * @return LoadTimeWeaver
     */
    @Bean(ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME)
    fun loadTimeWeaver(): LoadTimeWeaver {
        var loadTimeWeaverToUse: LoadTimeWeaver? = null

        // 如果配置了自定义的LoadTimeWeaver的话，那么获取自己配置的LoadTimeWeaver
        if (this.loadTimeWeaverConfigurer != null) {
            loadTimeWeaverToUse = this.loadTimeWeaverConfigurer!!.getLoadTimeWeaver()
        }

        // 如果没有配置自定义的LoadTimeWeaver的话，那么需要创建一个默认的LoadTimeWeaver
        if (loadTimeWeaverToUse == null) {
            loadTimeWeaverToUse = DefaultContextLoadTimeWeaver(classLoader!!)
        }

        // 解析注解当中的AspectJWeaving的模式，AUTO-开启，AUTODETECT-探测aop配置文件来判断是否开启，DISABLE-关闭
        val metadata = this.annotationMetadata
        if (metadata != null) {
            val aspectJWeaving = metadata.getAnnotations().get(EnableAspectJWeaving::class.java)
            val mode = aspectJWeaving.getEnum("aspectJWeaving", AspectJWeaving::class.java)
            if (mode == AspectJWeaving.ENABLED) {
                AspectJWeavingEnabler.enableAspectJWeaving(loadTimeWeaverToUse, this.classLoader!!)
            } else if (mode == AspectJWeaving.AUTODETECT) {
                val resource = this.classLoader!!.getResource(AspectJWeavingEnabler.ASPECTJ_AOP_XML_RESOURCE)
                if (resource != null) {
                    AspectJWeavingEnabler.enableAspectJWeaving(loadTimeWeaverToUse, this.classLoader!!)
                }
            }
        }
        return loadTimeWeaverToUse
    }


}