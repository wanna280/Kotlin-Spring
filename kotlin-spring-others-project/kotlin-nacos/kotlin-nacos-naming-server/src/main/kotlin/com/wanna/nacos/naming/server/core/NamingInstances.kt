package com.wanna.nacos.naming.server.core

import com.wanna.nacos.api.naming.pojo.Record

/**
 * 维护了Nacos的NamingInstance列表
 *
 * @param instanceList 实例列表
 */
data class NamingInstances(val instanceList: List<NamingInstance>) : Record