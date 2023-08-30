package com.wanna.debugger.bistoury.instrument.proxy.generator

/**
 * ID生成器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/6/24
 */
interface IdGenerator {

    /**
     * 生成ID
     *
     * @return 生成的ID
     */
    fun generateId(): String
}