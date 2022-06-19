package com.wanna.mybatis.spring.app.entity

class User {
    var id: Int = -1
    var name: String = ""
    var age: Int = -1
    override fun toString(): String = "User(id=$id, name='$name', age=$age)"
}
