package com.wanna.boot.test.context

import com.wanna.framework.test.context.BootstrapWith
import com.wanna.framework.test.context.junit.jupiter.SpringExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

/**
 * 标识这是一个SpringBoot的测试类
 *
 * * 1.将会使用[SpringBootTestContextBootstrapper]去引导整个TestContext的启动；
 * * 2.将会使用[SpringExtension]去进行扩展JUnit5
 */
@BootstrapWith(SpringBootTestContextBootstrapper::class)
@ExtendWith(SpringExtension::class)
annotation class SpringBootTest(
    val value: Array<String> = [],
    val properties: Array<String> = [],
    val args: Array<String> = [],
    val classes: Array<KClass<*>> = [],
    val webEnvironment: WebEnvironment = WebEnvironment.MOCK
) {
    enum class WebEnvironment {
        MOCK,
        RANDOM_PORT,
        DEFINED_PORT,
        NONE
    }
}
