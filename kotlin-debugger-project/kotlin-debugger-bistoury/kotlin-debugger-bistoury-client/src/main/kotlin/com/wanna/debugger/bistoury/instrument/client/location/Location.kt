package com.wanna.debugger.bistoury.instrument.client.location

/**
 * 描述Java源代码当中一个类当中的某个代码行
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param sourceJavaFile 原始的Java文件的文件路径(例如"com/wanna/Test.java")
 * @param lineNumber Java文件的行号
 */
data class Location(val sourceJavaFile: String, val lineNumber: Int)