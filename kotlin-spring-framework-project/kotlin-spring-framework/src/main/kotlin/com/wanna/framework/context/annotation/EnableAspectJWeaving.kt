package com.wanna.framework.context.annotation

/**
 * 标识这个注解，将会去开启AspectJ的加载时的编织，基于JavaAgent去技术去拦截一个类的定义工作
 *
 * @see LoadTimeWeavingConfigurer
 * @see LoadTimeWeavingConfiguration
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Import([LoadTimeWeavingConfiguration::class])  // 导入完成加载时去进行编织的配置类
annotation class EnableAspectJWeaving(
    val aspectJWeaving: AspectJWeaving = AspectJWeaving.AUTODETECT  // 编制模式
) {
    /**
     * AspectJ的自动编织的模式，开启(Enabled)、关闭(Disabled)、自动探测(AutoDetect)
     */
    enum class AspectJWeaving {
        ENABLED, DISABLED, AUTODETECT
    }
}