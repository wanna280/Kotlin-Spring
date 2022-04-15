package com.wanna.framework.test

import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.context.annotations.Autowired

@Component
class User {

    @Autowired
    private var phone: Phone? = null
}

@Component
class Phone {

    @Autowired
    private var user: User? = null

}