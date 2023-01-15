package com.wanna.framework.test.context

import kotlin.reflect.KClass

/**
 * 自定义Spring TestContext过程当中需要使用到的[TestExecutionListener]列表
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 *
 * @param value 需要使用的TestExecutionListener的类, 同listeners属性
 * @param listeners 需要使用的TestExecutionListener的类, 同value属性
 */
annotation class TestExecutionListeners(
    val value: Array<KClass<out TestExecutionListener>> = [],
    val listeners: Array<KClass<out TestExecutionListener>> = []
)