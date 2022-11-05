package com.wanna.framework.test.context

import kotlin.reflect.KClass

/**
 * 测试环境下的需要激活的Profile
 *
 * @param value 需要激活的profile
 * @param profiles 需要激活的profile
 * @param resolver 自定义的ActiveProfiles的解析器
 */
annotation class ActiveProfiles(
    val value: Array<String> = [],
    val profiles: Array<String> = [],
    val resolver: KClass<out ActiveProfilesResolver> = ActiveProfilesResolver::class
)
