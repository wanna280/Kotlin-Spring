package com.wanna.framework.context.annotation

/**
 * 这是一个可以去进行配置的Condition，支持去获取配置的阶段(ConfigurationPhase)
 *
 * @see ConfigurationPhase
 */
interface ConfigurationCondition : Condition {

    /**
     * 获取Condition需要生效的阶段
     *
     * @return Condition生效的阶段("PARSE_CONFIGURATION"/"REGISTER_BEAN")
     */
    fun getConfigurationPhase(): ConfigurationPhase

    /**
     * Condition生效阶段的枚举值
     */
    enum class ConfigurationPhase { PARSE_CONFIGURATION, REGISTER_BEAN }
}