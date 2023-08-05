package com.wanna.debugger.bistoury.instrument.client.debugger.bean

/**
 * 记录ASM访问一个方法栈帧当中的局部变量时的相关信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/30
 *
 * @param name 局部变量表当中的变量名
 * @param descriptor 描述符
 * @param start 该局部变量的有效作用范围的行号start
 * @param end 该局部变量的有效作用范围的行号end
 * @param index 该局部变量在方法栈帧局部变量表当中的索引
 */
data class LocalVariable(val name: String, val descriptor: String, val start: Int, val end: Int, val index: Int)