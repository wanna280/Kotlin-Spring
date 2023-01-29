package com.wanna.framework.core.metrics

import java.util.function.Supplier

/**
 * 这是ApplicationStartup的默认实现
 *
 * @see ApplicationStartup
 * @see StartupStep
 */
open class DefaultApplicationStartup : ApplicationStartup {

    override fun start(name: String): StartupStep {
        return DefaultStep()
    }

    /**
     * 这是一个默认的Step的实现, 什么都为空
     *
     * @see StartupStep
     */
    open class DefaultStep : StartupStep {
        // 是否已经记录完成了? 
        private var recorded = false

        private val defaultTags = DefaultTags()

        override fun getName(): String {
            return "default"
        }

        override fun getId(): Long {
            return 0L
        }

        override fun getParentId(): Long? {
            return null
        }

        override fun tag(key: String, value: String): StartupStep {
            if (this.recorded) {
                throw IllegalArgumentException("当前步骤已经结束了, 不能继续执行tag")
            }
            return this
        }

        override fun tag(key: String, value: Supplier<String>): StartupStep {
            if (this.recorded) {
                throw IllegalArgumentException("当前步骤已经结束了, 不能继续执行tag")
            }
            return this
        }

        override fun getTags(): StartupStep.Tags {
            return defaultTags
        }

        override fun end() {
            this.recorded = true
        }

        /**
         * 默认的Tags实现
         */
        class DefaultTags : StartupStep.Tags {
            override fun iterator(): Iterator<StartupStep.Tag> {
                return emptyList<StartupStep.Tag>().iterator()
            }
        }
    }
}