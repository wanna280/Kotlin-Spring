package com.wanna.framework.context.support

import com.wanna.framework.beans.annotations.Bean
import com.wanna.framework.beans.annotations.Ordered
import com.wanna.framework.beans.annotations.PriorityOrdered
import com.wanna.framework.context.AbstractApplicationContext
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.DefaultListableBeanFactory
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.context.processor.beans.MergedBeanDefinitionPostProcessor
import com.wanna.framework.context.processor.beans.internal.ApplicationListenerDetector
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor
import com.wanna.framework.core.OrderComparator

/**
 * 这是一个执行PostProcessor的委托类，可以委托去完成BeanFactoryPostProcessor和BeanPostProcessor的执行
 */
class PostProcessorRegistrationDelegate private constructor() {

    companion object {

        /**
         * 执行BeanFactoryPostProcessors
         *
         * @param postProcessors 在ApplicationContext当中已经注册的BeanFactoryPostProcessor列表
         * @param beanFactory BeanFactory
         */
        @JvmStatic
        fun invokeBeanFactoryPostProcessors(
            beanFactory: ConfigurableListableBeanFactory, postProcessors: List<BeanFactoryPostProcessor>
        ) {
            // 已经处理过的BeanFactoryPostProcessor的beanName列表
            val processedBeans = HashSet<String>()
            // 1.首先要处理所有的BeanDefinitionRegistryPostProcessor的执行
            if (beanFactory is BeanDefinitionRegistry) {
                // 常规的BeanFactoryPostProcessor
                val regularProcessors = ArrayList<BeanFactoryPostProcessor>()
                // BeanDefinitionRegistryPostProcessor列表
                val registryPostProcessors = ArrayList<BeanDefinitionRegistryPostProcessor>()

                // 1.1 需要调用通过API往容器中添加的BeanDefinitionRegistryPostProcessor，并保存常规的BeanFactoryPostProcessor
                // 它的优先级比自己放在容器中的BenFactoryPostProcessor拥有更高的优先级
                postProcessors.forEach {
                    if (it is BeanDefinitionRegistryPostProcessor) {
                        it.postProcessBeanDefinitionRegistry(beanFactory)
                        registryPostProcessors.add(it)
                    } else {
                        regularProcessors.add(it)
                    }
                }

                // 当前阶段要执行的BeanRegistryPostProcessor列表
                val currentRegistryPostProcessors = ArrayList<BeanDefinitionRegistryPostProcessor>()
                // 从容器中拿到所有的BeanRegistryPostProcessor
                val postProcessorNames =
                    beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor::class.java)

                // 1.2 从容器中的所有的BeanDefinitionRegistryPostProcessor中找到所有实现了PriorityOrdered接口的Bean进行排序执行
                postProcessorNames.forEach { beanName ->
                    if (beanFactory.isTypeMatch(beanName, PriorityOrdered::class.java)) {
                        currentRegistryPostProcessors.add(
                            beanFactory.getBean(
                                beanName, BeanDefinitionRegistryPostProcessor::class.java
                            )!!
                        )
                        // 标识这个Bean已经被处理
                        processedBeans.add(beanName)
                    }
                }

                // 排序并执行
                sortProcessors(currentRegistryPostProcessors, beanFactory)
                invokeBeanDefinitionRegistryPostProcessors(currentRegistryPostProcessors, beanFactory)
                registryPostProcessors.addAll(currentRegistryPostProcessors)
                currentRegistryPostProcessors.clear()  // clear掉

                // 1.3 从容器中的所有的BeanDefinitionRegistryPostProcessor中找到所有实现了Ordered接口的Bean进行排序执行
                postProcessorNames.forEach { beanName ->
                    if (!processedBeans.contains(beanName) && beanFactory.isTypeMatch(
                            beanName, Ordered::class.java
                        )
                    ) {
                        currentRegistryPostProcessors.add(
                            beanFactory.getBean(
                                beanName, BeanDefinitionRegistryPostProcessor::class.java
                            )!!
                        )
                        // 标识这个Bean已经被处理
                        processedBeans.add(beanName)
                    }
                }

                // 排序并执行
                sortProcessors(currentRegistryPostProcessors, beanFactory)
                invokeBeanDefinitionRegistryPostProcessors(currentRegistryPostProcessors, beanFactory)
                registryPostProcessors.addAll(currentRegistryPostProcessors)
                currentRegistryPostProcessors.clear()  // clear掉

                // 1.4  从容器中的所有的BeanDefinitionRegistryPostProcessor中找到不是Ordered也不是PriorityOrdered的Bean进行排序执行
                postProcessorNames.forEach { beanName ->
                    if (!processedBeans.contains(beanName)) {
                        currentRegistryPostProcessors.add(
                            beanFactory.getBean(
                                beanName, BeanDefinitionRegistryPostProcessor::class.java
                            )!!
                        )
                        // 标识这个Bean已经被处理
                        processedBeans.add(beanName)
                    }
                }

                // 执行(没有Ordered，不用进行排序)所有的普通的BeanDefinitionRegistryPostProcessor
                invokeBeanDefinitionRegistryPostProcessors(currentRegistryPostProcessors, beanFactory)
                registryPostProcessors.addAll(currentRegistryPostProcessors)
                currentRegistryPostProcessors.clear()  // clear掉

                // 执行所有的BeanDefinitionRegistryPostProcessor的postProcessBeanFactory方法
                invokeBeanFactoryPostProcessors(registryPostProcessors, beanFactory)

                // 执行所有的通过API方式加入的正常BeanFactoryPostProcessor
                invokeBeanFactoryPostProcessors(regularProcessors, beanFactory)
            }

            // 完成BeanRegistryDefinitionPostProcessor的处理之后，需要完成普通的BeanFactoryPostProcessor的执行
            val postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor::class.java)

            // 分别找出PriorityOrdered，Ordered，以及NonOrdered的BeanFactoryPostProcessor并进行分开
            val priorityOrderedProcessors = ArrayList<BeanFactoryPostProcessor>()
            val orderedProcessors = ArrayList<BeanFactoryPostProcessor>()
            val nonOrderedProcessors = ArrayList<BeanFactoryPostProcessor>()
            postProcessorNames.forEach { beanName ->
                if (processedBeans.contains(beanName)) {  // skip because it has been processed
                    return
                }
                val postProcessor = beanFactory.getBean(beanName, BeanFactoryPostProcessor::class.java)!!
                if (beanFactory.isTypeMatch(beanName, PriorityOrdered::class.java)) {
                    priorityOrderedProcessors += postProcessor
                } else if (beanFactory.isTypeMatch(beanName, Ordered::class.java)) {
                    orderedProcessors += postProcessor
                } else {
                    nonOrderedProcessors += postProcessor
                }
            }

            // 排序并执行所有的PriorityOrdered的BeanFactoryPostProcessor
            sortProcessors(priorityOrderedProcessors, beanFactory)
            invokeBeanFactoryPostProcessors(priorityOrderedProcessors, beanFactory)

            // 排序并执行所有的Ordered的BeanFactoryPostProcessor
            sortProcessors(orderedProcessors, beanFactory)
            invokeBeanFactoryPostProcessors(orderedProcessors, beanFactory)

            // 执行所有的普通的BeanFactoryPostProcessor
            invokeBeanFactoryPostProcessors(nonOrderedProcessors, beanFactory)
        }

        /**
         * 执行给定的所有给定的BeanDefinitionRegistryPostProcessor
         */
        @JvmStatic
        private fun invokeBeanDefinitionRegistryPostProcessors(
            postProcessors: Collection<BeanDefinitionRegistryPostProcessor>, registry: BeanDefinitionRegistry
        ) {
            postProcessors.forEach { processor -> processor.postProcessBeanDefinitionRegistry(registry) }
        }

        /**
         * 执行给定的所有的BeanFactoryPostProcessor
         */
        @JvmStatic
        private fun invokeBeanFactoryPostProcessors(
            postProcessors: Collection<BeanFactoryPostProcessor>, beanFactory: ConfigurableListableBeanFactory
        ) {
            postProcessors.forEach { processor -> processor.postProcessBeanFactory(beanFactory) }
        }

        /**
         * 注册所有的BeanPostProcessor
         */
        @JvmStatic
        fun registerBeanPostProcessors(
            beanFactory: ConfigurableListableBeanFactory, applicationContext: AbstractApplicationContext
        ) {

            val postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor::class.java)

            val priorityOrderedProcessors = ArrayList<BeanPostProcessor>()
            val orderedProcessors = ArrayList<BeanPostProcessor>()
            val nonOrderedProcessors = ArrayList<BeanPostProcessor>()
            val internalProcessors = ArrayList<BeanPostProcessor>()

            postProcessorNames.forEach { beanName ->
                val postProcessor = beanFactory.getBean(beanName, BeanPostProcessor::class.java)!!
                if (beanFactory.isTypeMatch(beanName, PriorityOrdered::class.java)) {
                    priorityOrderedProcessors += postProcessor
                } else if (beanFactory.isTypeMatch(beanName, Ordered::class.java)) {
                    orderedProcessors += postProcessor
                } else {
                    nonOrderedProcessors += postProcessor
                }
                // 如果是MergedBeanDefinitionPostProcessor
                if (postProcessor is MergedBeanDefinitionPostProcessor) {
                    internalProcessors += postProcessor
                }
            }
            // 注册所有PriorityOrdered的BeanPostProcessor
            sortProcessors(priorityOrderedProcessors, beanFactory)
            registerBeanPostProcessors(priorityOrderedProcessors, beanFactory)

            // 注册所有Ordered的BeanPostProcessor
            sortProcessors(orderedProcessors, beanFactory)
            registerBeanPostProcessors(orderedProcessors, beanFactory)

            // 注册所有的正常的BeanPostProcessor
            registerBeanPostProcessors(nonOrderedProcessors, beanFactory)

            // 注册所有的internalBeanPostProcessor，保证它一定是最后被执行的...
            sortProcessors(internalProcessors, beanFactory)
            registerBeanPostProcessors(internalProcessors, beanFactory)

            // 添加ApplicationListenerDetector，完成ApplicationListener的检测并注册到容器当中
            beanFactory.addBeanPostProcessor(ApplicationListenerDetector(applicationContext))
        }

        /**
         * 将给定的BeanPostProcessor列表，全部注册到BeanFactory当中
         */
        @JvmStatic
        private fun registerBeanPostProcessors(
            postProcessors: List<BeanPostProcessor>, beanFactory: ConfigurableListableBeanFactory
        ) {
            // 注册BeanPostProcessor
            postProcessors.forEach { processor -> beanFactory.addBeanPostProcessor(processor) }
        }

        /**
         * 对BeanFactoryPostProcessor/BeanPostProcessor去进行排序
         */
        @JvmStatic
        private fun sortProcessors(processors: MutableList<*>, beanFactory: ConfigurableListableBeanFactory) {
            if (processors.size <= 1) {
                return
            }
            var comparatorToUse: Comparator<Any?>? = null
            // 如果在beanFactory当中指定了自定义的依赖比较器，那么采用容器中给定的
            // 如果beanFactory当中没有指定自定义的依赖比较器，那么采用默认的(OrderComparator)
            if (beanFactory is DefaultListableBeanFactory) {
                comparatorToUse = beanFactory.getDependencyComparator()
            }
            comparatorToUse = comparatorToUse ?: OrderComparator.INSTANCE
            processors.sortedWith(comparatorToUse)
        }
    }
}