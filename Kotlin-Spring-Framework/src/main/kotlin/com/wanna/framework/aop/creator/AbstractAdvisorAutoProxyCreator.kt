package com.wanna.framework.aop.creator

import com.wanna.framework.aop.Advisor
import com.wanna.framework.aop.PointcutAdvisor
import com.wanna.framework.aop.TargetSource
import com.wanna.framework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper
import com.wanna.framework.aop.support.AopUtils
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.util.ReflectionUtils

/**
 * 它为AbstractAutoProxyCreator提供了获取Advisor的来源，尝试去beanFactory当中去探测Advisor；
 * 在探寻到的所有Advisor之后，挨个去进行比较，判断它能否应用给当前正在创建的Bean
 *
 * @see AbstractAutoProxyCreator
 */
abstract class AbstractAdvisorAutoProxyCreator : AbstractAutoProxyCreator() {

    // 协助去进行BeanFactory当中的Advisor进行寻找
    private var advisorRetrievalHelper: BeanFactoryAdvisorRetrievalHelper? = null

    /**
     * 实现父类的模板方法，提供Advisor的获取
     *
     * @param beanClass beanClass
     * @param beanName beanName
     * @param targetSource targetSource
     */
    override fun getAdvicesAndAdvisorsForBean(
        beanClass: Class<*>, beanName: String, targetSource: TargetSource?
    ): Array<Any>? {
        val advisors = findEligibleAdvisors(beanClass, beanName)
        return if (advisors.isEmpty()) DO_NOT_PROXY else advisors.toTypedArray()
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        super.setBeanFactory(beanFactory)
        if (beanFactory !is ConfigurableListableBeanFactory) {
            throw IllegalArgumentException("beanFactory必须是ConfigurableListableBeanFactory类型")
        }
        initBeanFactory(beanFactory)
    }

    protected open fun initBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // 这里需要返回一个Adapter，因为要将isEligibleBean方法转接到对于Advisor的isEligibleBean当中
        advisorRetrievalHelper = BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory)
    }

    /**
     * 寻找有资格针对于指定的Bean去产生代理的Advisor列表
     * @see findCandidateAdvisors
     * @see findAdvisorsThatCanApply
     * @see extendsAdvisors
     */
    open fun findEligibleAdvisors(beanClass: Class<*>, beanName: String): List<Advisor> {
        // 1.找出所有的候选的Advisor列表
        val candidateAdvisors = findCandidateAdvisors()

        // 2.找出可以进行应用的Advisor，主要是使用ClassFilter去对类进行匹配，使用MethodMatcher去遍历所有的方法去进行匹配
        val eligibleAdvisors = ArrayList(findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName))

        // 3.扩展Advisor列表，钩子方法，交给子类去进行实现
        extendsAdvisors(eligibleAdvisors)

        // 4,完成Advisor的排序并返回
        return if (eligibleAdvisors.isNotEmpty()) sortAdvisors(eligibleAdvisors) else eligibleAdvisors
    }

    /**
     * 自定义扩展Advisor逻辑，这是是一个模板方法，交给子类去进行扩展
     */
    protected open fun extendsAdvisors(eligibleAdvisors: MutableList<Advisor>) {

    }

    /**
     * 完成Advisor的排序，默认排序规则为按照Order的注解以及Ordered注解的方式去进行排序
     */
    protected open fun sortAdvisors(advisors: MutableList<Advisor>): MutableList<Advisor> {
        AnnotationAwareOrderComparator.sort(advisors)
        return advisors
    }

    /**
     * 寻找候选的Advisor列表，默认实现方式为从容器当中获取到所有的Advisor的Bean列表
     * 在子类当中，可以去进行重写，实现从别的来源当中去产生Advisor，比如使用AspectJ相关的注解
     */
    protected open fun findCandidateAdvisors(): MutableList<Advisor> {
        return advisorRetrievalHelper!!.findAdvisorBeans()
    }

    /**
     * 找出可以进行应用给当前的Bean的Advisor，主要是使用ClassFilter去对类进行匹配，使用MethodMatcher去遍历所有的方法去进行匹配
     *
     * @param advisors 要去进行匹配的Advisor列表
     * @param beanClass beanClass
     * @param beanName beanName
     * @return 匹配到的可以应用给当前Bean的Advisor列表
     */
    protected open fun findAdvisorsThatCanApply(
        advisors: MutableList<Advisor>, beanClass: Class<*>, beanName: String
    ): List<Advisor> {
        return AopUtils.findAdvisorsThatCanApply(advisors, beanClass)
    }

    /**
     * 是否是一个有资格成为Advisor的Bean
     */
    protected open fun isEligibleBean(name: String): Boolean {
        return true
    }

    /**
     * 这是一个BeanFactoryAdvisorRetrievalHelperAdapter，用来桥接去执行对于AutoProxyCreator的isEligibleBean方法去判断是否是有资格的Bean
     * 这个类必须标识为inner class，也就是普通的内部类(不能是static内部类)，因为它要访问外部类的对象
     * @see BeanFactoryAdvisorRetrievalHelper
     */
    inner class BeanFactoryAdvisorRetrievalHelperAdapter(_beanFactory: ConfigurableListableBeanFactory) :
        BeanFactoryAdvisorRetrievalHelper(_beanFactory) {

        /**
         * 在Kotlin当中，通过this@OuterClassName的方式，去访问外部类对象，等价于Java当中的OuterClassName.this
         */
        override fun isEligibleBean(name: String): Boolean {
            return this@AbstractAdvisorAutoProxyCreator.isEligibleBean(name)
        }
    }
}