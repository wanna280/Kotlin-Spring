package com.wanna.nacos.config.server.service.dump

import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.stereotype.Service
import com.wanna.nacos.api.core.GlobalExecutor
import com.wanna.nacos.config.server.service.ConfigCacheService
import com.wanna.nacos.config.server.service.dump.processor.DumpAllProcessor
import com.wanna.nacos.config.server.service.repository.PersistService
import javax.annotation.PostConstruct

/**
 * 嵌入式的DumpService
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

    /**
     * 添加一个轮询任务, 一直执行dump任务, 执行PersistService去将配置文件信息去同步到ConfigCacheService当中
     *
     * @see ConfigCacheService
     */
    @PostConstruct
    override fun init() {
        GlobalExecutor.executeByCommon {
            while (true) {
                dumpOperate(dumpAllProcessor)
            }
        }
    }
}