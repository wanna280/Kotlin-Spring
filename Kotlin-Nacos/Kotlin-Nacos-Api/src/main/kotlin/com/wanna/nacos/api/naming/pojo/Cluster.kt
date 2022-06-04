package com.wanna.nacos.api.naming.pojo

import com.wanna.framework.core.util.StringUtils
import com.wanna.nacos.api.naming.ValidateBase

/**
 * Nacos的集群
 */
open class Cluster(val clusterName: String) : java.io.Serializable, ValidateBase {
    var serviceName: String = ""  // serviceName
    var metadata = HashMap<String, String>()  // metadata

    override fun validate() {
        if (!StringUtils.hasText(serviceName)) {
            throw IllegalStateException("serviceName不能为空")
        }
    }
}