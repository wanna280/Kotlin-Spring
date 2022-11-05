package com.wanna.framework.test.context

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 */
interface TestContextBootstrapper {
    fun setBootstrapContext(bootstrapContext: BootstrapContext)

    fun getBootstrapContext(): BootstrapContext

    fun buildTestContext(): TestContext

    fun buildMergedContextConfiguration(): MergedContextConfiguration

    fun getTestExecutionListeners(): List<TestExecutionListener>
}