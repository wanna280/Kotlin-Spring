package com.wanna.boot.devtools.restart

import com.wanna.boot.autoconfigure.condition.ConditionOutcome
import com.wanna.boot.autoconfigure.condition.SpringBootCondition
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 去匹配Restarter是否已经完成初始化工作的Condition
 *
 * @see ConditionalOnInitializedRestarter
 */
class OnInitializedRestarterCondition : SpringBootCondition() {

    /**
     * 检查[Restarter]是否已经完成初始化? 只有在[Restarter]已经完成初始化之后,
     * 才算是匹配成功, 才需要启用DevTools的热部署的相关功能
     *
     * @param context context
     * @param metadata metadata
     * @return 如果匹配, return [ConditionOutcome.match]; 不匹配的话, return [ConditionOutcome.noMatch]
     */
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val restarter = Restarter.getInstance() ?: return ConditionOutcome.noMatch("Restarter还没完成创建")
        restarter.getInitialUrls() ?: return ConditionOutcome.noMatch("Restarter的InitialUrls为空")
        return ConditionOutcome.match("Restarter已经完成创建, 并且Restarter的InitialUrls已经完成初始化")
    }
}