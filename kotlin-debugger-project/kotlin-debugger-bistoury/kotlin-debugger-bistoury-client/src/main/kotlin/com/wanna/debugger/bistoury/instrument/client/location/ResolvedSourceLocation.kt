package com.wanna.debugger.bistoury.instrument.client.location

/**
 * 根据给定的Java文件以及对应的行号, 去解析该位置的代码的详细信息(比如某个类当中的某一行对应的方法信息)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 *
 * @param classSignature 类的签名(例如"Lcom/wanna/Test$InnerClass;")
 * @param adjustedLineNumber 类的源码行号
 * @param methodDescriptor 方法描述符(返回值&参数列表)
 * @param methodName 该行号的代码位置所处的方法名
 */
data class ResolvedSourceLocation(
    val classSignature: String,
    val adjustedLineNumber: Int,
    val methodName: String,
    val methodDescriptor: String
)