package com.wanna.nacos.naming.server.consistency

import com.wanna.nacos.api.naming.pojo.Record
import com.wanna.nacos.api.naming.pojo.consistency.Datum

/**
 * 提供一致性的实现的Service
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
interface ConsistencyService {

    fun put(key: String, record: Record)

    fun remove(key: String)

    fun get(key: String): Datum<*>

    fun listen(key: String, listener: RecordListener)

    fun unListen(key: String, listener: RecordListener)

    fun isAvailable(): Boolean
}