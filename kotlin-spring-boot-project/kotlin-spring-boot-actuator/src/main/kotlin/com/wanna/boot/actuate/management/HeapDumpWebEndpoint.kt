package com.wanna.boot.actuate.management

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.boot.actuate.endpoint.annotation.ReadOperation
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.lang.Nullable
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import java.lang.management.PlatformManagedObject
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * HeapDump的Endpoint
 */
@Endpoint("heapdump")
open class HeapDumpWebEndpoint {

    // HeapDumper
    private var heapDumper: HeapDumper? = null

    /**
     * 对外提供HeapDump的读操作的Endpoint
     */
    @ReadOperation
    open fun heapDump(@Nullable live: Boolean?): ByteArray {
        return dumpHeap(live ?: true)
    }

    /**
     * 将堆相关的信息去dump成为ByteArray
     *
     * @param live 是否只dump可达的对象
     * @return HeapDump的结果
     */
    private fun dumpHeap(live: Boolean): ByteArray {
        if (this.heapDumper == null) {
            this.heapDumper = HotSpotDiagnosticMXBeanHeapDumper()
        }

        val tempFile = createTempFile(live)
        this.heapDumper!!.dump(tempFile, live)
        return tempFile.readBytes()
    }

    @Throws(IOException::class)
    private fun createTempFile(live: Boolean): File {
        val date = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm").format(LocalDateTime.now())
        val file = File.createTempFile("heapdump" + date + if (live) "-live" else "", ".hprof")
        file.delete()
        return file
    }

    /**
     * HeapDumper, 提供对于Java堆当中的相关信息的Dump到一个文件当中的策略接口
     */
    interface HeapDumper {

        /**
         * 将堆信息去dump到指定的文件当中
         *
         * @param file 堆信息将会被dump到这个文件当中
         * @param live 是不是只有可达的对象才需要dump? 
         */
        fun dump(file: File, live: Boolean)
    }

    /**
     * HotSpot的HeadDumper
     */
    open class HotSpotDiagnosticMXBeanHeapDumper : HeapDumper {
        // 初始化相关的MXBean
        var diagnosticMXBean: Any? = null

        // 用来Dump Heap的目标方法
        var dumpHeapMethod: Method? = null

        init {
            // 找到相关的MXBean
            val diagnosticMXBeanClass = ClassUtils.forName<Any?>("com.sun.management.HotSpotDiagnosticMXBean")

            // 初始化diagnosticMXBean
            @Suppress("UNCHECKED_CAST")
            this.diagnosticMXBean = ManagementFactory
                .getPlatformMXBean(diagnosticMXBeanClass as Class<PlatformManagedObject>)

            // 反射的方式去找到dumpHeap的方法
            dumpHeapMethod = ReflectionUtils.findMethod(
                diagnosticMXBeanClass, "dumpHeap",
                String::class.java,
                java.lang.Boolean.TYPE
            )
        }

        override fun dump(file: File, live: Boolean) {
            ReflectionUtils.invokeMethod(this.dumpHeapMethod!!, this.diagnosticMXBean!!, file.absolutePath, live)
        }
    }
}