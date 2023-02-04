package com.wanna.debugger.bistoury.instrument.client.debugger.bean

import com.wanna.debugger.bistoury.instrument.client.location.Location
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 对于一个Debug断点信息的封装
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param id 断点ID
 * @param codeLocation 断点的位置(类/行号)
 * @param condition 断点条件
 */
data class Breakpoint(val id: String, val codeLocation: Location, val condition: String) {

    /**
     * 是否已经触发该断点的标志
     */
    private val triggered = AtomicBoolean()

    /**
     * 触发断点
     *
     * @return 是否触发成功?
     */
    fun trigger(): Boolean = triggered.compareAndSet(false, true)
}