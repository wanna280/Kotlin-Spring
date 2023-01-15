package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.stereotype.Repository
import com.wanna.framework.context.stereotype.Service
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * 对于单元测试当中, 应该如何去加载(load)/配置(configure)一个Spring的[ApplicationContext]？
 *
 * # 支持的资源类型如下
 * * 1.可以通过value/locations属性去指定一个Spring的XML配置文件; 
 * * 2.可以通过[ContextLoader]可以支持去导入XML的配置文件加载成为一个[ApplicationContext],
 * 对于[SmartContextLoader]则新增了可以使用注解的方式去加载成为一个[ApplicationContext]的方式
 *
 *
 * # 支持下面这些类型的配置类(通过classes属性去进行配置)
 * * 1.一个标注了[Configuration]注解的配置类; 
 * * 2.一个Spring的Component(例如标注了[Component]、[Service]、[Repository]这些注解类); 
 * * 3.任意标注了[Bean]注解的类
 *
 * @see ApplicationContext
 * @see ApplicationContextInitializer
 * @see ContextLoader
 * @see SmartContextLoader
 *
 * @param value 需要导入的XML配置文件, 同locations属性
 * @param locations 需要导入的XML配置文件, 同value属性
 * @param classes 需要导入的SpringComponent的类
 * @param initializers ApplicationContext的Initializer
 * @param loader ContextLoader, 提供对于ApplicationContext的加载
 */
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class ContextConfiguration(
    val value: Array<String> = [],
    val locations: Array<String> = [],
    val classes: Array<KClass<*>> = [],
    val initializers: Array<KClass<out ApplicationContextInitializer<out ConfigurableApplicationContext>>> = [],
    val loader: KClass<out ContextLoader> = ContextLoader::class
)