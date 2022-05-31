package com.wanna.nacos.api.naming.pojo

/**
 * Nacos的集群
 */
open class Cluster(val clusterName: String) {
    lateinit var serviceName: String  // serviceName
    var metadata = HashMap<String, String>()  // metadata
}