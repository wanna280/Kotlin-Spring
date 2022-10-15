package com.wanna.mybatis.spring.app.entity

data class User(var id: Int = -1, var name: String = "", var age: Int = -1) {
    constructor() : this(-1, "", -1)
}
