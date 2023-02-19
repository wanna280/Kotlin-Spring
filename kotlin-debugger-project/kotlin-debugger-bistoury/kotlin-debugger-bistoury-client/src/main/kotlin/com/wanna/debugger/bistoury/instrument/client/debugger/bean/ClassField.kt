package com.wanna.debugger.bistoury.instrument.client.debugger.bean

/**
 * 记录ASM访问类当中的一个方法时的相关信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/30
 *
 * @param access 方法的访问修饰符
 * @param name 方法名
 * @param descriptor 方法的描述符(参数/返回值信息)
 */
data class ClassField(val access: Int, val name: String, val descriptor: String)