package com.wanna.framework.context.annotation

import com.wanna.framework.core.io.support.PropertySourceFactory
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass


/**
 * 给SpringBeanFactory当中去导入一个配置文件，添加到环境当中(目前支持使用properties配置文件)
 *
 * @param locations locations(同value)，配置文件的路径
 * @param value locations(同locations)
 * @param name PropertySourceName
 * @param factory PropertySourceFactory，提供去创建PropertySource的回调(默认情况是支持使用Properties的配置文件)
 */
@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PropertySource(
    @get:AliasFor("locations")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val locations: Array<String> = [],
    val name: String = "",
    val factory: KClass<out PropertySourceFactory> = PropertySourceFactory::class
)
