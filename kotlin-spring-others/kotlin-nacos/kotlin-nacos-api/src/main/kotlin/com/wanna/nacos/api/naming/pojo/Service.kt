package com.wanna.nacos.api.naming.pojo

import com.wanna.framework.util.StringUtils
import com.wanna.nacos.api.naming.ValidateBase

/**
 * 这是Nacos的一个服务
 */
open class Service : java.io.Serializable, ValidateBase {
    var groupName: String = ""  // groupName
    var serviceName: String = ""  // serviceName
    var metadata = HashMap<String, String>()  // metadata

    open fun set(groupName: String, serviceName: String) {
        this.groupName = groupName
        this.serviceName = serviceName
    }

    override fun validate() {
        if (!StringUtils.hasText(groupName)) {
            throw IllegalStateException("groupName不能为空")
        }
        if (!StringUtils.hasText(serviceName)) {
            throw IllegalStateException("serviceName不能为空")
        }
    }
}