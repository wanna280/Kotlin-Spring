package com.wanna.boot.context.config

import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable

/**
 * 标准的ConfigDataResource的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see StandardConfigDataReference
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

}