package com.wanna.nacos.config.server.service.dump

import com.wanna.framework.context.stereotype.Service
import com.wanna.nacos.api.core.GlobalExecutor
import com.wanna.nacos.config.server.service.dump.processor.DumpAllProcessor
import com.wanna.nacos.config.server.service.repository.PersistService
import javax.annotation.PostConstruct

/**
 * 嵌入式的DumpService
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param persistService PersistService, 自动注入
 */
@Service
open class EmbeddedDumpService(persistService: PersistService) : DumpService(persistService) {
    /**
     * DumpAll的Processor
     */
    private lateinit var dumpAllProcessor: DumpAllProcessor

    /**
     * 初始化时, 自动执行的方法, 完成初始化操作
     */
    @PostConstruct
    override fun init() {
        this.dumpAllProcessor = DumpAllProcessor(this)
        GlobalExecutor.executeByCommon {
            dumpOperate(dumpAllProcessor)
        }
    }
}