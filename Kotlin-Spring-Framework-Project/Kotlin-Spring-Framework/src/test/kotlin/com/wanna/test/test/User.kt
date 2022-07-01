package com.wanna.test.test

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.beans.factory.annotation.Value

@Component
class User {

    @Value("name")
    var name: String? = null;

    @Autowired
    var phone: Phone? = null

    @Autowired
    var applicationContext: ApplicationContext? = null
}

@Component
class Phone {

    @Autowired
    private var user: User? = null

}