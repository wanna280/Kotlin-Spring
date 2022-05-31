package com.wanna.nacos.api.naming.pojo

/**
 * 这是Nacos的一个服务
 */
open class Service {
    lateinit var groupName: String  // groupName
    lateinit var serviceName: String  // serviceName
    var metadata = HashMap<String, String>()  // metadata

    open fun set(groupName: String, serviceName: String) {
        this.groupName = groupName
        this.serviceName = serviceName
    }

    open fun validate() {
        if (!this::groupName.isInitialized || !this::serviceName.isInitialized) {
            throw IllegalStateException("Service未完成初始化，请先完成初始化")
        }
    }
}