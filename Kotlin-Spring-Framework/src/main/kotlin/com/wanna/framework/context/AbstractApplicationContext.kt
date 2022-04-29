package com.wanna.framework.context

import com.wanna.framework.context.event.ApplicationEventPublisher
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationContextAwareProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationListenerDetector
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.context.support.PostProcessorRegistrationDelegate
import com.wanna.framework.core.environment.EnvironmentCapable

/**
 * 提供了一个抽象的ApplicationContext都是实现
 */
abstract class AbstractApplicationContext : ConfigurableApplicationContext, ListableBeanFactory,
    ApplicationEventPublisher, EnvironmentCapable {

    // 存放BeanFactoryPostProcessor的列表
    private val beanFactoryPostProcessors: MutableList<BeanFactoryPostProcessor> = ArrayList()

    // 存放监听器列表
    val applicationListeners: MutableList<ApplicationListener<*>> = ArrayList()

    override fun refresh() {
        // 准备完成容器的刷新工作
        prepareRefresh()

        // 获取(创建)BeanFactory
        val beanFactory = obtainBeanFactory()

        // 完成BeanFactory的初始化
        prepareBeanFactory(beanFactory)

        // BeanFactory的后置处理工作，是一个钩子方法，交给子类去进行完成
        postProcessBeanFactory(beanFactory)

        // 执行所有的BeanFactoryPostProcessor
        invokeBeanFactoryPostProcessors(beanFactory)

        // 注册BeanPostProcessor
        registerBeanPostProcessors(beanFactory)

        // 初始化事件多播器
        initApplicationEventMulticaster(beanFactory)

        // 初始化容器当中需要用到的MessageSource，主要是和国际化相关的，这里没提供相关的实现...
        initMessageSource()

        // 交给子类去完成，子类可以在这里去完成Tomcat等对象的创建...
        onRefresh()

        // 注册监听器
        registerListeners()

        // 完成剩下的所有单实例Bean的初始化工作
        finishBeanFactoryInitialization(beanFactory)

        // 完成容器的刷新
        finishRefresh()
    }

    /**
     * 准备完成容器的刷新功能
     */
    protected open fun prepareRefresh() {

    }

    /**
     * 获取BeanFactory，返回的是一个ConfigurableListableBeanFactory
     */
    protected abstract fun obtainBeanFactory(): ConfigurableListableBeanFactory

    /**
     * 完成BeanFactoryPostProcessor的执行
     */
    protected open fun invokeBeanFactoryPostProcessors(beanFactory: ConfigurableListableBeanFactory) {
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, beanFactoryPostProcessors)
    }

    /**
     * 完成所有的BeanPostProcessor的注册工作
     */
    protected open fun registerBeanPostProcessors(beanFactory: ConfigurableListableBeanFactory) {
        PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this)
    }

    /**
     * 完成MessageSource的初始化，和国际化相关
     */
    protected open fun initMessageSource() {

    }

    /**
     * 完成事件多拨器的初始化，如果容器中已经有了，那么使用容器中的作为要使用的，如果容器中没有，那么将会采用吗默认的事件多拨器
     */
    protected open fun initApplicationEventMulticaster(beanFactory: ConfigurableListableBeanFactory) {

    }

    /**
     * 完成BeanFactory的准备工作，给BeanFactory当中添加一些相关的依赖
     */
    protected open fun prepareBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 给容器中注册可以被解析的依赖，包括BeanFactory，Application，ApplicationEventPublisher等
        beanFactory.registerResolvableDependency(BeanFactory::class.java, beanFactory)
        beanFactory.registerResolvableDependency(ApplicationContext::class.java, this)
        beanFactory.registerResolvableDependency(ApplicationEventPublisher::class.java, this)

        // 添加ApplicationContext的BeanPostProcessor，完成BeanClassLoaderAware/EnvironmentAware等Aware接口的处理
        this.addBeanPostProcessor(ApplicationContextAwareProcessor(this))

        // 添加ApplicationListener的Detector，完成EventListener的探测和注册
        this.addBeanPostProcessor(ApplicationListenerDetector(this))
    }

    /**
     * 对BeanFactory完成后置处理工作
     */
    protected open fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

    }

    /**
     * 钩子方法，交给子类去完成
     */
    protected open fun onRefresh() {

    }

    /**
     * 完成容器刷新之后需要完成的处理工作
     */
    protected open fun finishRefresh() {

    }

    /**
     * 完成所有的监听器的注册
     */
    protected open fun registerListeners() {

    }

    /**
     * 完成BeanFactory的初始化，剩下的所有未进行实例化的Bean都会在这里去进行该Bean的实例化和初始化工作
     */
    protected open fun finishBeanFactoryInitialization(beanFactory: ConfigurableListableBeanFactory) {
        // 如果容器当中没有嵌入式的值解析器，那么需要往容器当中加入一个默认的
        if (!getBeanFactory().hasEmbeddedValueResolver()) {
            getBeanFactory().addEmbeddedValueResolver(object : StringValueResolver {
                override fun resolveStringValue(strVal: String) = getEnvironment().resolveRequiredPlaceholders(strVal)
            })
        }

        // 完成剩下的所有单实例Bean的实例化和初始化工作
        beanFactory.preInstantiateSingletons()
    }

    /**
     * 添加BeanFactoryPostProcessor
     */
    override fun addBeanFactoryPostProcessor(processor: BeanFactoryPostProcessor) {
        beanFactoryPostProcessors += processor
    }

    override fun publishEvent(event: Any) {
        TODO("Not yet implemented")
    }

    override fun getBean(beanName: String): Any? {
        return obtainBeanFactory().getBean(beanName)
    }

    override fun <T> getBean(beanName: String, type: Class<T>): T? {
        return obtainBeanFactory().getBean(beanName, type)
    }

    override fun <T> getBean(type: Class<T>): T? {
        return obtainBeanFactory().getBean(type)
    }

    override fun isSingleton(beanName: String): Boolean {
        return obtainBeanFactory().isSingleton(beanName)
    }

    override fun isPrototype(beanName: String): Boolean {
        return obtainBeanFactory().isPrototype(beanName)
    }

    override fun addBeanPostProcessor(processor: BeanPostProcessor) {
        obtainBeanFactory().addBeanPostProcessor(processor)
    }

    override fun removeBeanPostProcessor(type: Class<*>) {
        obtainBeanFactory().removeBeanPostProcessor(type)
    }

    override fun removeBeanPostProcessor(index: Int) {
        obtainBeanFactory().removeBeanPostProcessor(index)
    }

    override fun isFactoryBean(beanName: String): Boolean {
        return obtainBeanFactory().isFactoryBean(beanName)
    }

    override fun isTypeMatch(beanName: String, type: Class<*>): Boolean {
        return obtainBeanFactory().isTypeMatch(beanName, type)
    }

    override fun getType(beanName: String): Class<*>? {
        return obtainBeanFactory().getType(beanName)
    }

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return obtainBeanFactory().getBeanNamesForType(type)
    }

    override fun <T> getBeansForType(type: Class<T>): Map<String, T> {
        return obtainBeanFactory().getBeansForType(type)
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        return obtainBeanFactory().getBeanNamesForTypeIncludingAncestors(type)
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        return obtainBeanFactory().getBeansForTypeIncludingAncestors(type)
    }

}