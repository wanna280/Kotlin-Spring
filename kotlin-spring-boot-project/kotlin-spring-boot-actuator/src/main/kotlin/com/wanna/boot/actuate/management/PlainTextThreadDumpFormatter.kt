package com.wanna.boot.actuate.management

import java.io.PrintWriter
import java.io.StringWriter
import java.lang.management.LockInfo
import java.lang.management.ManagementFactory
import java.lang.management.MonitorInfo
import java.lang.management.ThreadInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * 将ThreadDump去进行格式化成文本的Formatter
 */
internal class PlainTextThreadDumpFormatter {
    fun format(threads: Array<ThreadInfo>): String {
        val dump = StringWriter()
        val writer = PrintWriter(dump)
        writePreamble(writer)
        for (info in threads) {
            writeThread(writer, info)
        }
        return dump.toString()
    }

    private fun writePreamble(writer: PrintWriter) {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        writer.println(dateFormat.format(LocalDateTime.now()))
        val runtime = ManagementFactory.getRuntimeMXBean()
        writer.printf(
            "Full thread dump %s (%s %s):%n", runtime.vmName, runtime.vmVersion,
            System.getProperty("java.vm.info")
        )
        writer.println()
    }

    private fun writeThread(writer: PrintWriter, info: ThreadInfo) {
        writer.printf("\"%s\" - Thread t@%d%n", info.threadName, info.threadId)
        writer.printf("   %s: %s%n", Thread.State::class.java.canonicalName, info.threadState)
        writeStackTrace(writer, info, info.lockedMonitors)
        writer.println()
        writeLockedOwnableSynchronizers(writer, info)
        writer.println()
    }

    private fun writeStackTrace(writer: PrintWriter, info: ThreadInfo, lockedMonitors: Array<MonitorInfo>) {
        var depth = 0
        for (element in info.stackTrace) {
            writeStackTraceElement(writer, element, info, lockedMonitorsForDepth(lockedMonitors, depth), depth == 0)
            depth++
        }
    }

    private fun lockedMonitorsForDepth(lockedMonitors: Array<MonitorInfo>, depth: Int): List<MonitorInfo> {
        return Stream.of(*lockedMonitors)
            .filter { lockedMonitor: MonitorInfo -> lockedMonitor.lockedStackDepth == depth }
            .collect(Collectors.toList())
    }

    private fun writeStackTraceElement(
        writer: PrintWriter, element: StackTraceElement, info: ThreadInfo,
        lockedMonitors: List<MonitorInfo>, firstElement: Boolean
    ) {
        writer.printf("\tat %s%n", element.toString())
        val lockInfo = info.lockInfo
        if (firstElement && lockInfo != null) {
            if (element.className == Any::class.java.name && element.methodName == "wait") {
                writer.printf("\t- waiting on %s%n", format(lockInfo))
            } else {
                val lockOwner = info.lockOwnerName
                if (lockOwner != null) {
                    writer.printf(
                        "\t- waiting to lock %s owned by \"%s\" t@%d%n", format(lockInfo), lockOwner,
                        info.lockOwnerId
                    )
                } else {
                    writer.printf("\t- parking to wait for %s%n", format(lockInfo))
                }
            }
        }
        writeMonitors(writer, lockedMonitors)
    }

    private fun format(lockInfo: LockInfo): String {
        return String.format("<%x> (a %s)", lockInfo.identityHashCode, lockInfo.className)
    }

    private fun writeMonitors(writer: PrintWriter, lockedMonitorsAtCurrentDepth: List<MonitorInfo>) {
        for (lockedMonitor in lockedMonitorsAtCurrentDepth) {
            writer.printf("\t- locked %s%n", format(lockedMonitor))
        }
    }

    private fun writeLockedOwnableSynchronizers(writer: PrintWriter, info: ThreadInfo) {
        writer.println("   Locked ownable synchronizers:")
        val lockedSynchronizers = info.lockedSynchronizers
        if (lockedSynchronizers == null || lockedSynchronizers.size == 0) {
            writer.println("\t- None")
        } else {
            for (lockedSynchronizer in lockedSynchronizers) {
                writer.printf("\t- Locked %s%n", format(lockedSynchronizer))
            }
        }
    }
}
