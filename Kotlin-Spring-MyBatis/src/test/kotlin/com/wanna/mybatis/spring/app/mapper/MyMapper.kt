package com.wanna.mybatis.spring.app.mapper

import com.wanna.mybatis.spring.app.entity.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface MyMapper {
    @Select("select * from test")
    fun select() : List<User>
}