package com.wanna.boot.actuate.autoconfigure.test.mx

import java.lang.management.ManagementFactory

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
class MXBeanTest {
}

fun main() {
    val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    println(operatingSystemMXBean.name)  // 操作系统的名字, 例如:"Mac OS X"
    println(operatingSystemMXBean.arch)  // CPU的体系结构, "X86_64"
    println(operatingSystemMXBean.availableProcessors)  // CPU的核数(逻辑核)
    println(operatingSystemMXBean.version)  // 操作系统的版本
    println(operatingSystemMXBean.systemLoadAverage)  // 操作系统的负载
    println(operatingSystemMXBean.objectName)  // java.lang:type=OperatingSystem

    val memoryMXBean = ManagementFactory.getMemoryMXBean()
    println(memoryMXBean.objectName)   // java.lang:type=Memory
    println(memoryMXBean.heapMemoryUsage)  // 堆内存的使用情况(eg: init = 536870912(524288K) used = 4194304(4096K) committed = 536870912(524288K) max = 8589934592(8388608K))
    println(memoryMXBean.nonHeapMemoryUsage)  // 非堆内存的使用情况(eg:init = 7667712(7488K) used = 10202280(9963K) committed = 16580608(16192K) max = -1(-1K))
    println(memoryMXBean.objectPendingFinalizationCount)  // 正在执行Finalization的对象数量

    val classLoadingMXBean = ManagementFactory.getClassLoadingMXBean()
    println(classLoadingMXBean.objectName)  // java.lang:type=ClassLoading
    println(classLoadingMXBean.loadedClassCount)  // 当前已经加载的类的数量
    println(classLoadingMXBean.unloadedClassCount)  // 已经卸载的类的数量
    println(classLoadingMXBean.totalLoadedClassCount)  // 一共加载的类的数量(unloadedClassCount+loadedClassCount)

    val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
    // println(runtimeMXBean.bootClassPath)
    println(runtimeMXBean.classPath)  // 获取ClassPath
    println(runtimeMXBean.libraryPath)  // 获取lib的Path
    println(runtimeMXBean.inputArguments)  // 获取命令行参数
    println(runtimeMXBean.name)  // pid@hostName
    println(runtimeMXBean.isBootClassPathSupported)  // false?
    println(runtimeMXBean.vmName)  // 虚拟机名称
    println(runtimeMXBean.vmVendor)  // vm厂商("Oracle Corporation")
    println(runtimeMXBean.specName)  // Java Virtual Machine Specification
    println(runtimeMXBean.specVersion)  // 11
    println(runtimeMXBean.specVendor)  // spec厂商("Oracle Corporation")
}