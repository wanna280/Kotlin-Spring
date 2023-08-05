package com.wanna.nacos.naming.server.consistency

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Component
import com.wanna.nacos.api.naming.pojo.Record
import com.wanna.nacos.api.naming.pojo.consistency.Datum
import com.wanna.nacos.naming.server.consistency.ephemeral.EphemeralConsistencyService
import com.wanna.nacos.naming.server.consistency.persistent.PersistentConsistencyService

/**
 * [ConsistencyService]的委托(Delegate)实现, 根据具体的规则去走持久/临时的[ConsistencyService]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
@Component("consistencyDelegate")
open class DelegateConsistencyServiceImpl : ConsistencyService {

    /**
     * 处理临时节点的[ConsistencyService]
     */
    @Autowired
    private lateinit var ephemeralConsistencyService: EphemeralConsistencyService

    /**
     * 处理持久节点的[ConsistencyService]
     */
    @Autowired
    private lateinit var persistentConsistencyService: PersistentConsistencyService

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