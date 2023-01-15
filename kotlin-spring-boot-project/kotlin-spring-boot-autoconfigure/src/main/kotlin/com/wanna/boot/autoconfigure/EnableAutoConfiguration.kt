package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 这是一个开启SpringBoot自动配置的注解, 标注这个注解会完成SpringApplication的自动配置;
 *
 * 它也是SpringFactories当中用于导入自动配置类的标识, 在"META-INF/spring.factories"当中通过如下的配置信息
 * "EnableAutoConfiguration=XXX"去给SpringBoot当中导入自动配置类列表
 *
 * @see SpringBootApplication
 *
 * @param exclude 要排除的配置类列表(Class)
 * @param excludeNames 要排除的配置类列表(className)
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import([AutoConfigurationImportSelector::class])
@AutoConfigurationPackage
annotation class EnableAutoConfiguration(val exclude: Array<KClass<*>> = [], val excludeNames: Array<String> = []) {
    companion object {
        // 是否开启自动配置的属性？通过配置文件将该属性设为false时, 即可关闭自动配置功能
        const val ENABLED_OVERRIDE_PROPERTY: String = "spring.boot.enableautoconfiguration"
    }
}
