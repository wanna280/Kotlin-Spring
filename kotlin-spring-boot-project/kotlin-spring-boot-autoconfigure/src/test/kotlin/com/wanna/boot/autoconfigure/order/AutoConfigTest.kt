package com.wanna.boot.autoconfigure.order

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.AutoConfigureBefore
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Configuration

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
@AutoConfigureAfter(value = [AutoConfig3::class])  // 3必须在1之前
@Configuration(proxyBeanMethods = false)
class AutoConfig1


@Configuration(proxyBeanMethods = false)
class AutoConfig2

@AutoConfigureBefore(value = [AutoConfig2::class])  // 2必须在3之后
@Configuration(proxyBeanMethods = false)
class AutoConfig3

@SpringBootApplication
class AutoConfigTest

fun main() {
    runSpringApplication<AutoConfigTest>()
}