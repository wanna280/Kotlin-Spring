package com.wanna.nacos.config.server.service.dump

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Service
import com.wanna.nacos.api.core.GlobalExecutor
import com.wanna.nacos.config.server.service.dump.processor.DumpAllProcessor
import com.wanna.nacos.config.server.service.repository.PersistService
import javax.annotation.PostConstruct

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
@Service
class EmbeddedDumpService : DumpService() {
    /**
     * DumpAll的Processor
     */
    private val dumpAllProcessor = DumpAllProcessor(this)

    /**
     * 注入PersistService
     *
     * @param persistService PersistService
     */
    @Autowired
    override fun setPersistService(persistService: PersistService) {
        super.setPersistService(persistService)
    }

    @PostConstruct
    override fun init() {
        GlobalExecutor.executeByCommon {
            while (true) {
                dumpOperate(dumpAllProcessor)
            }
        }
    }
}