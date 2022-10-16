package com.wanna.test.xml.bean

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
class User {
    var name: String? = null

    var machine: Machine? = null

    fun init() {
        println("init user...")
    }

    override fun toString(): String {
        return "User(name=$name, machine=$machine)"
    }
}