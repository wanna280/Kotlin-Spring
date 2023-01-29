package com.wanna.framework.test.junit4

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.test.JUnitApp
import com.wanna.framework.test.context.ContextConfiguration
import com.wanna.framework.test.context.junit4.SpringRunner
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
@ContextConfiguration(classes = [JUnitApp::class])
@RunWith(SpringRunner::class)
class JUnit4Test {

    @Autowired
    private lateinit var processors: Map<String, BeanPostProcessor>

    @Autowired
    private var app: JUnitApp? = null

    @Test
    fun test() {
        println(processors)
    }
}