package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.AutoConfigurationMetadata
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.type.AnnotatedTypeMetadata


/**
 * 这是一个基于Class的Condition，它主要用来处理@ConditionOnClass/@ConditionOnMissingClass等注解
 *
 * @see ConditionOnClass
 * @see ConditionOnMissingClass
 * @see FilteringSpringBootCondition
 * @see SpringBootCondition
 */
@Order(Ordered.ORDER_HIGHEST)
@Suppress("UNCHECKED_CAST")
open class OnClassCondition : FilteringSpringBootCondition() {

    /**
     * 匹配metadata当中配置的configurationClassName.OnClassCondition配置的className列表是否存在
     * 需要newThread去执行一半的任务，最终再将两个线程执行的任务merge到outcomes当中进行return 即可
     */
    override fun getOutcomes(
        autoConfigurationClasses: Array<String?>, autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<ConditionOutcome?> {
        val size = autoConfigurationClasses.size

        // Spring官方在这里将OnClassCondition的匹配设计成为了两个线程并发执行的逻辑，因为可能对OnClassCondition去进行匹配需要花费大量的时间
        // 在最后执行一下Merge的操作，去将两个线程的匹配结果去进行合并，并进行return
        // Spring官方测试：采用两个线程将会拥有最好的效果，采用更多的线程去进行匹配可能会得到更差的结果
        if (size > 1 && Runtime.getRuntime().availableProcessors() > 1) {
            val split = size ushr 1  // get split point，split=size>>>1

            // 将任务从split位置拆分成为两段，并且第二段交给另外一个线程去进行执行
            val firstResolver = StandardOutcomeResolver(autoConfigurationClasses, 0, split, autoConfigurationMetadata)
            val secondResolver =
                StandardOutcomeResolver(autoConfigurationClasses, split, size, autoConfigurationMetadata)
            val threadedResolver = ThreadedOutcomeResolver(secondResolver)

            // 当前线程执行前一部分Outcomes的匹配
            val firstOutcomes = firstResolver.resolveOutcomes()  // execute 0..split
            // 等待另外一个线程执行完另一部分的Outcomes的匹配
            val secondOutcomes = threadedResolver.resolveOutcomes()  // execute split..size

            // merge，使用System.arraycopy将两个线程的执行结果merge到outcomes列表当中
            val outcomes = arrayOfNulls<ConditionOutcome?>(size)
            System.arraycopy(firstOutcomes, 0, outcomes, 0, firstOutcomes.size)
            System.arraycopy(secondOutcomes, 0, outcomes, split, secondOutcomes.size)
            return outcomes
        }
        // 如果只有一个元素或者处理器数量只有1个，那么不用新创建一个线程去解析，那样性能会变得更低
        return StandardOutcomeResolver(autoConfigurationClasses, 0, 1, autoConfigurationMetadata).resolveOutcomes()
    }

    /**
     * 这是一个OutcomeResolver，完成对ConditionOutcome的解析
     */
    interface OutcomesResolver {
        fun resolveOutcomes(): Array<ConditionOutcome?>
    }

    /**
     * 这是一个标准的StandardOutcomeResolver，支持去对start..end部分的configurationClass去进行ConditionOnClass的解析
     */
    inner class StandardOutcomeResolver(
        private val autoConfigurationClasses: Array<String?>,
        private val start: Int,
        private val end: Int,
        private val autoConfigurationMetadata: AutoConfigurationMetadata
    ) : OutcomesResolver {
        override fun resolveOutcomes(): Array<ConditionOutcome?> {
            // fixed:如果给的数据越界了，那么直接return empty
            if (start >= autoConfigurationClasses.size || end <= start) {
                return emptyArray()
            }
            val outcomes = arrayOfNulls<ConditionOutcome?>(end - start)  // size=end-start
            for (index in start until end) {
                val autoConfigurationClass = autoConfigurationClasses[index]
                if (autoConfigurationClass != null) {  // pass null entry
                    // 检查ClassCondition的所有className是否都已经存在
                    val onClassTypes = autoConfigurationMetadata.getSet(autoConfigurationClass, "OnClassCondition")
                    // base=start，offset=index-start
                    outcomes[index - start] = getOutcome(onClassTypes, ConditionOnClass::class.java)
                }
            }
            return outcomes
        }
    }

    /**
     * 基于线程的OutcomeResolver，传递一个StandardOutcomeResolver，在初始化时即启动线程
     */
    inner class ThreadedOutcomeResolver(outcomesResolver: OutcomesResolver) : OutcomesResolver {
        private var outcomes: Array<ConditionOutcome?>? = null

        private var thread = Thread { outcomes = outcomesResolver.resolveOutcomes() }

        init {
            thread.start()
        }

        /**
         * 在之前已经启动过线程去执行Outcomes的解析了，这里需要做的是，join等着线程执行完成
         */
        override fun resolveOutcomes(): Array<ConditionOutcome?> {
            thread.join()  // join for result
            return outcomes!!
        }
    }

    /**
     * 获取Condition的匹配结果，判断requiredBeanTypes列表当中的所有的className是否都已经存在？
     *
     * @param requiredBeanTypes 需要去进行匹配的className列表
     * @return 如果要匹配的所有className都已经存在，那么return null；如果存在有className不存在的，那么return noMatch
     */
    private fun getOutcome(requiredBeanTypes: Set<String>?, annotation: Class<out Annotation>): ConditionOutcome? {
        val missing = filter(requiredBeanTypes, ClassNameFilter.MISSING, this.getClassLoader())
        if (missing.isNotEmpty()) {  // noMatch
            return ConditionOutcome.noMatch()
        }
        return null  // match
    }

    /**
     * 获取@ConditionOnClass和@ConditionOnMissingClass两个注解，去完成匹配
     */
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val message = ConditionMessage.empty()
        // 匹配@ConditionOnClass
        if (metadata.isAnnotated(ConditionOnClass::class.java.name)) {
            val onClassAttrs = metadata.getAnnotationAttributes(ConditionOnClass::class.java.name)

            // 获取用户配置的所有classNames
            val classNames = (onClassAttrs["name"] as Array<String>).toMutableList()
            classNames += (onClassAttrs["value"] as Array<Class<*>>).map { it.name }.toList()

            // 过滤出来所有的missing的className
            val misssing = filter(classNames, ClassNameFilter.MISSING, context.getClassLoader())

            // 如果missing不为空，那么说明，确实有missing的，但是需求应该是全部都得present，应该return false
            if (misssing.isNotEmpty()) {
                return ConditionOutcome.noMatch("ConditionOnClass不匹配")
            }
        }
        // 匹配@ConditionOnMissingClass
        if (metadata.isAnnotated(ConditionOnMissingClass::class.java.name)) {
            val onMissingClassAttrs = metadata.getAnnotationAttributes(ConditionOnMissingClass::class.java.name)
            // 获取用户配置的所有classNames
            val classNames = (onMissingClassAttrs["value"] as Array<String>).toMutableList()

            // 过滤出来所有的present的className
            val present = filter(classNames, ClassNameFilter.PRESENT, context.getClassLoader())

            // 如果present不为空，那么说明，确实有present的，但是需求是全部都得missing，那么return false
            if (present.isNotEmpty()) {
                return ConditionOutcome.noMatch("ConditionOnMissingClass不匹配")
            }
        }
        return ConditionOutcome.match(message)
    }
}