package com.wanna.framework.test.context

/**
 * [TestContext]的Bootstrapper, 负责引导[TestContext]的创建和启动
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 *
 * @see com.wanna.framework.test.context.support.AbstractTestContextBootstrapper
 */
interface TestContextBootstrapper {

    /**
     * 设置BootstrapContext
     *
     * @param bootstrapContext BootstrapContext
     */
    fun setBootstrapContext(bootstrapContext: BootstrapContext)

    /**
     * 获取BootstrapContext
     *
     * @return BootstrapContext
     */
    fun getBootstrapContext(): BootstrapContext

    /**
     * 根据testClass、MergedContextConfiguration等去构建出来[TestContext]
     *
     * @return TestContext
     */
    fun buildTestContext(): TestContext

    /**
     * 根据testClass上的[ContextConfiguration]注解去构建出来[MergedContextConfiguration]
     *
     * @return MergedContextConfiguration
     */
    fun buildMergedContextConfiguration(): MergedContextConfiguration

    /**
     * 获取所有的监听[TestContext]的执行的生命周期的[TestExecutionListener]监听器列表; 
     * 通过解析[TestExecutionListeners]注解去进行获取用户自定义的[TestExecutionListener],
     * 如果无法获取到用户自定义的[TestExecutionListener]的话, 将会尝试从SpringFactories当中去
     * 进行加载到合适的[TestExecutionListener], 并利用DependencyComparator去完成排序
     *
     * @return TestExecutionListener列表
     */
    fun getTestExecutionListeners(): List<TestExecutionListener>
}