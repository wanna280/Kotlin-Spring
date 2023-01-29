package com.wanna.nacos.config.server.model

/**
 * ConfigInfo
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 *
 * @param tenant tenant(namespace)
 * @param appName appName
 * @param type fileType
 */
open class ConfigInfo(
    dataId: String?,
    group: String?,
    content: String?,
    id: Long = 0L,
    var tenant: String?,
    var appName: String?,
    var type: String?
) : ConfigInfoBase(dataId, group, content, id) {

    constructor(dataId: String?, group: String?, content: String?) : this(
        dataId, group, content,
        0L, null, null, null
    )

    constructor(dataId: String?, group: String?, content: String?, appName: String?, tenant: String?) : this(
        dataId, group, content,
        0L, tenant, appName, null
    )

    constructor() : this(null, null, null)
}