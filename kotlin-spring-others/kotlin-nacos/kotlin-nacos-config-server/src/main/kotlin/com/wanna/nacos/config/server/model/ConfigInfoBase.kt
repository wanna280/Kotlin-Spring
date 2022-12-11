package com.wanna.nacos.config.server.model

import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.utils.Md5Utils

/**
 * ConfigInfo的一些基础信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 *
 * @param dataId dataId
 * @param group group
 * @param content 配置文件的内容
 * @param id id
 */
open class ConfigInfoBase(
    var dataId: String?,
    var group: String?,
    var content: String?,
    var id: Long = 0L
) {

    /**
     * 配置文件的MD5值
     */
    var md5: String? = null

    init {
        // 如果指定了内容的话, 那么我们去生成MD5值去进行保存
        if (this.content != null) {
            this.md5 = Md5Utils.md5Hex(content!!, Constants.ENCODE)
        }
    }

    constructor() : this(null, null, null, 0L)
    constructor(dataId: String?, group: String?, content: String?) : this(dataId, group, content, 0L)
}