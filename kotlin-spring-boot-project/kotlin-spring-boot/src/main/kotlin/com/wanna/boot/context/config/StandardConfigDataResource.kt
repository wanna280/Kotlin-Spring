package com.wanna.boot.context.config

import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable

/**
 * 标准的ConfigDataResource的实现, 通过组合一个[StandardConfigDataReference]和一个最终加载到的[Resource]去进行实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see StandardConfigDataReference
 *
 * @param reference 指向配置文件的Reference引用
 * @param resource 根据Reference去加载到的配置文件
 * @param emptyDirectory 是否是一个空文件夹?
 */
class StandardConfigDataResource(
    val reference: StandardConfigDataReference,
    val resource: Resource,
    val emptyDirectory: Boolean = false
) : ConfigDataResource(true) {
    constructor(reference: StandardConfigDataReference, resource: Resource) : this(reference, resource, false)

    /**
     * 当前资源文件对应的Profile
     */
    @Nullable
    val profile: String?
        get() = reference.profile

    /**
     * equals方法, 通过比较Resource的方式去进行实现
     */
    override fun equals(@Nullable other: Any?): Boolean {
        // 引用相等, return true
        if (this === other) {
            return true
        }
        // 类不同, return false
        if (other == null || other.javaClass != this.javaClass) {
            return false
        }

        // 比较Resource&emptyDirectory是否相等
        return (other as StandardConfigDataResource).resource == this.resource && other.emptyDirectory == this.emptyDirectory
    }

    /**
     * hashCode, 直接使用Resource的hashCode去进行生成
     */
    override fun hashCode(): Int = this.resource.hashCode()

    /**
     * toString, 采用Resource的toString去进行生成
     *
     * @return toString
     */
    override fun toString(): String = resource.toString()
}