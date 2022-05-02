package com.wanna.framework.context.annotation

/**
 * 这是一个可以去进行配置的Condition，支持去获取配置的阶段(ConfigurationPhase)
 */
interface ConfigurationCondition : Condition {
    fun getConfigurationPhase(): ConfigurationPhase

    enum class ConfigurationPhase { PARSE_CONFIGURATION, REGISTER_BEAN }
}