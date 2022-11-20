package com.wanna.nacos.naming.server.consistency.ephemeral.distro

import com.wanna.framework.context.stereotype.Component
import com.wanna.nacos.api.naming.pojo.Record
import com.wanna.nacos.api.naming.pojo.consistency.Datum
import com.wanna.nacos.naming.server.consistency.RecordListener
import com.wanna.nacos.naming.server.consistency.ephemeral.EphemeralConsistencyService

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@Component("distroConsistencyService")
open class DistroConsistencyServiceImpl : EphemeralConsistencyService {
    override fun put(key: String, record: Record) {
        TODO("Not yet implemented")
    }

    override fun remove(key: String) {
        TODO("Not yet implemented")
    }

    override fun get(key: String): Datum<*> {
        TODO("Not yet implemented")
    }

    override fun listen(key: String, listener: RecordListener) {
        TODO("Not yet implemented")
    }

    override fun unListen(key: String, listener: RecordListener) {
        TODO("Not yet implemented")
    }

    override fun isAvailable(): Boolean {
        TODO("Not yet implemented")
    }
}