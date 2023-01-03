package com.wanna.framework.test.context.junit.jupiter

import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.annotation.AliasFor
import com.wanna.framework.test.context.ContextConfiguration
import com.wanna.framework.test.context.ContextLoader
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * 标识这是一个Spring的JUnit配置类
 *
 * @param value 需要导入的XML配置文件，同locations属性
 * @param locations 需要导入的XML配置文件，同value属性
 * @param classes 需要导入的SpringComponent的类
 * @param initializers ApplicationContext的Initializer
 * @param loader ContextLoader，提供对于ApplicationContext的加载
 *
 * @see ContextConfiguration
 * @see SpringExtension
 * @see ExtendWith
 */
@Inherited
@ContextConfiguration
@ExtendWith(SpringExtension::class)
annotation class SpringJUnitConfig(
    @get:AliasFor("value", annotation = ContextConfiguration::class)
    val value: Array<String> = [],
    @get:AliasFor("locations", annotation = ContextConfiguration::class)
    val locations: Array<String> = [],
    @get:AliasFor("classes", annotation = ContextConfiguration::class)
    val classes: Array<KClass<*>> = [],
    @get:AliasFor("initializers", annotation = ContextConfiguration::class)
    val initializers: Array<KClass<out ApplicationContextInitializer<out ConfigurableApplicationContext>>> = [],
    @get:AliasFor("loader", annotation = ContextConfiguration::class)
    val loader: KClass<out ContextLoader> = ContextLoader::class
)
