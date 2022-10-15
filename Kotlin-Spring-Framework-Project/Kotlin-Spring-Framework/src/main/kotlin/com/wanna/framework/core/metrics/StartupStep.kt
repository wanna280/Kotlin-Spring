package com.wanna.framework.core.metrics

import java.util.function.Supplier

/**
 * 这是记录SpringApplication启动过程当中的一个步骤
 *
 * @see ApplicationStartup
 */
interface StartupStep {

    /**
     * stepName
     */
    fun getName(): String

    /**
     * stepId
     */
    fun getId(): Long

    /**
     * parentStepId
     */
    fun getParentId(): Long?

    /**
     * 在当前步骤当中的打上tag
     *
     * @param key key Of Tag
     * @param value value Of Tag
     * @return Step
     */
    fun tag(key: String, value: String): StartupStep

    /**
     * 在当前步骤当中打上tag
     * @param key key Of Tag
     * @param value value supplier
     */
    fun tag(key: String, value: Supplier<String>): StartupStep

    /**
     * 获取这个步骤当中的Tags
     *
     * @return tags
     */
    fun getTags(): Tags

    /**
     * 结束这个step
     */
    fun end()


    /**
     * Tags，可用于进行迭代的Tag
     */
    interface Tags : Iterable<Tag>

    /**
     * 简单地去存储这个步骤当中的key/value元信息
     */
    interface Tag {
        fun getKey(): String
        fun getValue(): String
    }

}