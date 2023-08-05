package com.wanna.framework.core.metrics

/**
 * 记录SpringApplication启动阶段的各个步骤, 在每个步骤当中, 可以记录下来相关的上下文数据到Step对象当中;
 * 可以利用中途当中打的Tag, 去进行整个系统的性能指标的监测等工作
 *
 * @see StartupStep
 */
interface ApplicationStartup {

    companion object {
        val DEFAULT: ApplicationStartup = DefaultApplicationStartup()
    }

    /**
     * 启动一个步骤, 并返回step对象; 一个步骤描述了当前的行动/阶段, 并且名字应该使用"."作为分割符的命名空间;
     * 并且该name还应该可以被重用, 在别人使用了别的实例的时候, 使用相同的stepName, 将会被描述为同一个step
     *
     * @param name 步骤的name
     * @return 该步骤对象
     */
    fun start(name: String): StartupStep
}