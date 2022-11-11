package com.wanna.nacos.api.naming.pojo

import com.wanna.framework.util.StringUtils
import com.wanna.nacos.api.naming.ValidateBase

/**
 * 用于描述需要去暴露到Nacos注册中心当中的一个服务
 */
open class Service : java.io.Serializable, ValidateBase {

    /**
     * groupName
     */
    var groupName: String = ""

    /**
     * serviceName(实际上应该为groupName::serviceName)
     */
    var serviceName: String = ""

    /**
     * metadata
     */
    var metadata = HashMap<String, String>()

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