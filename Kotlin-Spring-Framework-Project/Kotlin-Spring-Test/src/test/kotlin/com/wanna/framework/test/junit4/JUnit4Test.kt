package com.wanna.framework.test.junit4

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.test.context.ContextConfiguration
import com.wanna.framework.test.context.junit4.SpringJUnit4ClassRunner
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
@ContextConfiguration(classes = [JUnitApp::class])
@RunWith(SpringJUnit4ClassRunner::class)
class JUnit4Test {

    @Autowired
    private lateinit var processors: Map<String, BeanPostProcessor>

    @Test
    fun test() {
        println(processors)
    }
}