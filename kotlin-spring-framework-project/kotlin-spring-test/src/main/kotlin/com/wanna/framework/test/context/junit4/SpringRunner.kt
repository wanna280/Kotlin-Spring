package com.wanna.framework.test.context.junit4

/**
 * [SpringJUnit4ClassRunner]的Delegate
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 *
 * @param testClass testClass
 * @see SpringJUnit4ClassRunner
 */
open class SpringRunner(testClass: Class<*>) : SpringJUnit4ClassRunner(testClass)