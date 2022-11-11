package com.wanna.framework.test.context

import com.wanna.framework.test.context.support.DefaultTestContextBootstrapper
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * 允许标注在类上去指定当前Spring的Test应用，应该如何去引导[TestContext]的启动？
 *
 * @param value 需要使用的[TestContextBootstrapper]的类
 *
 * @see TestContextBootstrapper
 */
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class BootstrapWith(val value: KClass<out TestContextBootstrapper> = DefaultTestContextBootstrapper::class)
