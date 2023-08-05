package com.wanna.debugger.bistoury.instrument.client.common

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.lang.instrument.ClassFileTransformer

/**
 * [ClassFileTransformer]的基础类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/1
 */
abstract class BaseClassFileTransformer : ClassFileTransformer {

    protected open fun computeFlag(classReader: ClassReader): Int {
        var flag = ClassWriter.COMPUTE_MAXS
        val version = classReader.readShort(6)
        if (version >= Opcodes.V1_7) {
            flag = ClassWriter.COMPUTE_FRAMES
        }
        return flag
    }
}