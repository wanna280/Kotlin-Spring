package com.wanna.boot.actuate.management

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.util.function.Function

/**
 * 将线程信息去进行dump的Dump的Endpoint
 *
 * @see ManagementFactory.getThreadMXBean
 */
@Endpoint("threaddump")
open class ThreadDumpEndpoint {
    // 将Thread信息Dump成为文本的Formatter
    private val formatter = PlainTextThreadDumpFormatter()

    /**
     * 获取线程的Dump信息
     *
     * @return 线程的Dump信息的描述符信息
     */
    @ReadOperation
    open fun threadDump(): ThreadDumpDescriptor = getFormattedThreadDump { ThreadDumpDescriptor(it.toList()) }

    /**
     * 获取格式化之后的ThreadDump信息
     *
     * @param formatter 如何去对ThreadInfo去进行格式化？
     * @return 格式化之后的数据
     */
    private fun <T> getFormattedThreadDump(formatter: Function<Array<ThreadInfo>, T>): T =
        formatter.apply(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true))

    /**
     * 线程的dump信息的描述符
     *
     * @param threads ThreadInfo
     */
    data class ThreadDumpDescriptor(val threads: List<ThreadInfo>)
}