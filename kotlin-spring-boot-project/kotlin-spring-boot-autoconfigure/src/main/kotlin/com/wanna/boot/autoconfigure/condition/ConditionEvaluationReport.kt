package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.annotation.Condition

/**
 * 记录配置类的条件匹配的细节信息, 去进行报告相关信息, 以及进行相关的日志输出
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/8
 */
class ConditionEvaluationReport {

    /**
     * 记录条件匹配的结果
     *
     * @param source 匹配的方法/类
     * @param condition Condition
     * @param outcome 条件匹配的结果
     */
    fun recordConditionEvaluation(source: String, condition: Condition, outcome: ConditionOutcome) {

    }

    companion object {

        /**
         * [ConditionEvaluationReport]的beanName
         */
        private const val BEAN_NAME = "autoConfigurationReport"

        /**
         * 从[beanFactory]当中去获取到[ConditionEvaluationReport]的工厂方法
         *
         * @param beanFactory BeanFactory
         * @return ConditionEvaluationReport
         */
        @JvmStatic
        fun get(beanFactory: ConfigurableListableBeanFactory): ConditionEvaluationReport {
            synchronized(beanFactory) {
                val report: ConditionEvaluationReport
                if (beanFactory.containsSingleton(BEAN_NAME)) {
                    report = beanFactory.getBean(BEAN_NAME, ConditionEvaluationReport::class.java)
                } else {
                    report = ConditionEvaluationReport()
                    beanFactory.registerSingleton(BEAN_NAME, report)
                }
                return report
            }
        }
    }

}