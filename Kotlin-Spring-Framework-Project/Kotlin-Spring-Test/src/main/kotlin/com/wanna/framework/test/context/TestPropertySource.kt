package com.wanna.framework.test.context

import java.lang.annotation.Inherited

/**
 * 测试环境当中导入PropertySource
 */
@Inherited
annotation class TestPropertySource(val locations: Array<String> = [])
