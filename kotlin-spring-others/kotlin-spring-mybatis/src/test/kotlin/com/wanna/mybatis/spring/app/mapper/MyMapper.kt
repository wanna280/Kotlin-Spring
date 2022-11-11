package com.wanna.mybatis.spring.app.mapper

import com.wanna.framework.context.stereotype.Component
import com.wanna.mybatis.spring.app.entity.User
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface MyMapper {
    @Select("select * from t_user")
    fun select(): List<User>

    @Insert("insert into t_user(id, name, age) values (#{id}, #{name}, #{age})")
    fun insert(user: User)
}

@Component
class MyMapperImpl : MyMapper {
    override fun select(): List<User> {
        TODO("Not yet implemented")
    }

    override fun insert(user: User) {
        TODO("Not yet implemented")
    }
}