package com.wanna.test.test

import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotations.Autowired
import com.wanna.framework.context.annotations.Value

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