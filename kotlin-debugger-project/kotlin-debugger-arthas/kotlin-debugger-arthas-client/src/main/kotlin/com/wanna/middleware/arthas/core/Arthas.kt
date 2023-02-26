package com.wanna.middleware.arthas.core

import com.sun.tools.attach.VirtualMachine
import com.sun.tools.attach.VirtualMachineDescriptor
import com.wanna.middleware.arthas.common.JavaVersionUtils
import com.wanna.middleware.arthas.core.config.Configure

/**
 * Arthas启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 *
 * @param args 命令行参数列表
 */
class Arthas private constructor(args: Array<String>) {

    init {
        val configure = parse(args)
        attachAgent(configure)
    }

    /**
     * 解析命令行参数
     *
     * @param args 命令行参数列表
     */
    private fun parse(args: Array<String>): Configure {
        return Configure()
    }

    /**
     * attachAgent
     *
     * @param configure attach的配置信息
     */
    private fun attachAgent(configure: Configure) {
        var virtualMachineDescriptor: VirtualMachineDescriptor? = null
        for (vmd in VirtualMachine.list()) {
            val pid = vmd.id()
            if (pid == configure.javaPid.toString()) {
                virtualMachineDescriptor = vmd
            }
        }
        var virtualMachine: VirtualMachine? = null

        try {
            if (virtualMachineDescriptor === null) {
                virtualMachine = VirtualMachine.attach(configure.javaPid.toString())
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor)
            }

            val targetVMSystemProperties = virtualMachine!!.systemProperties
            val targetVMJavaVersion = JavaVersionUtils.getJavaVersionStr(targetVMSystemProperties)
            val currentVMJavaVersion = JavaVersionUtils.getJavaVersionStr()
            if (currentVMJavaVersion != null && targetVMJavaVersion != null) {
                if (currentVMJavaVersion != targetVMJavaVersion) {
                    // log warn
                }
            }

            val arthasAgentPath = configure.arthasAgent ?: throw IllegalStateException("arthas agent is null")

            // load agent
            virtualMachine.loadAgent(arthasAgentPath, configure.arthasCore + ";" + configure.toString())
        } finally {
            virtualMachine?.detach()
        }
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            com.wanna.middleware.arthas.core.Arthas(args)
        }
    }
}