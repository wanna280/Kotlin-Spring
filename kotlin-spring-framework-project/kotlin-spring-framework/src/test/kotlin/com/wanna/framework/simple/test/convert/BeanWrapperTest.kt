package com.wanna.framework.simple.test.convert

import com.wanna.framework.beans.BeanWrapperImpl

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/16
 */
class BeanWrapperTest {
}

class User {
    var info = UserInfo()
}

class UserInfo {
    val address = arrayOf("shanghai1", "beijing")
    val address2 = setOf("shanghai1", "beijing")
    val address3 = listOf("shanghai1", "beijing")
}

fun main() {
    test1()
    test2()
}


private fun test2() {
    val beanWrapper2 = BeanWrapperImpl(User())
    val propertyValueInfo = beanWrapper2.getPropertyValue("info.address[1]")
    val propertyValueInfo2 = beanWrapper2.getPropertyValue("info.address2[1]")
    val propertyValueInfo3 = beanWrapper2.getPropertyValue("info.address3[1]")
    assert(propertyValueInfo == propertyValueInfo2)
    assert(propertyValueInfo == propertyValueInfo3)
}
private fun test1() {
    val beanWrapper = BeanWrapperImpl(UserInfo())

    val propertyValue = beanWrapper.getPropertyValue("address[1]")
    val propertyValue2 = beanWrapper.getPropertyValue("address2[1]")
    val propertyValue3 = beanWrapper.getPropertyValue("address3[1]")
    assert(propertyValue == propertyValue2)
    assert(propertyValue == propertyValue3)

}