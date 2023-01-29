package com.wanna.framework.scheduling.annotation

import com.wanna.framework.context.annotation.Import
import com.wanna.framework.core.Ordered
import kotlin.reflect.KClass

/**
 * 提供异步的支持, 导入异步相关的处理器;
 * 使用方式如下, 即可开启Spring的异步支持, 就可以使用Spring的`@Async`方法让该方法成为一个异步方法
 *
 * ```kotlin
 *     @EnableAsync
 *     @Component
 *     class App {
 *
 *     }
 * ```
 *
 * @see Async
 *
 * @param order @Async的处理器的优先级
 * @param annotation 要使用的Async注解, 默认为@Async
 */
@Target(AnnotationTarget.CLASS)
@Import([AsyncConfigurationSelector::class])
annotation class EnableAsync(
    val order: Int = Ordered.ORDER_LOWEST,
    val annotation: KClass<out Annotation> = Async::class,
    val proxyTargetClass: Boolean = false
)
