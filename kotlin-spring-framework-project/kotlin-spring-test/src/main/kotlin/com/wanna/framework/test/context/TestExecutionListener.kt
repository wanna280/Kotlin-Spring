package com.wanna.framework.test.context

/**
 * Test任务执行的监听器，在JUnit的测试任务的各个周期，去对[TestContext]去进行自定义
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/4
 */
interface TestExecutionListener {

    /**
     * 在`@BeforeTestClass`(JUnit5的`@BeforeAll`)方法执行之前的回调
     *
     * @param testContext TestContext
     */
    fun beforeTestClass(testContext: TestContext)

    /**
     * 在`@AfterClass`(JUnit5的`@AfterAll`)方法执行之后的回调
     *
     * @param testContext TestContext
     */
    fun afterTestClass(testContext: TestContext)

    /**
     * 在testInstance准备时的回调
     *
     * @param testContext TestContext
     */
    fun prepareTestInstance(testContext: TestContext)

    /**
     * 在`@Before`(JUnit5的`@BeforeEach`)方法执行之前的回调
     *
     * @param testContext TestContext
     */
    fun beforeTestMethod(testContext: TestContext)

    /**
     * 在`@After`(JUnit5的`@AfterEach`)方法执行之后的回调
     *
     * @param testContext TestContext
     */
    fun afterTestMethod(testContext: TestContext)

    /**
     * 在一个`@Test`方法执行之前的回调
     *
     * @param testContext TestContext
     */
    fun beforeTestExecution(testContext: TestContext)

    /**
     * 在一个`@Test`方法执行之后的回调
     *
     * @param testContext TestContext
     */
    fun afterTestExecution(testContext: TestContext)
}