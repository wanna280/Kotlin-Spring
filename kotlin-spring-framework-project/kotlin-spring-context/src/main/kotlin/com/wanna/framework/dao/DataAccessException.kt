package com.wanna.framework.dao

import com.wanna.framework.core.NestedRuntimeException

/**
 * 数据访问异常.
 *
 * 这个异常的所有子类, 旨在让用户去寻找和处理数据访问的异常, 而无需去关注具体的数据访问的API(例如JDBC相关的API).
 * 因此, 可以在不知道使用JDBC的情况下对乐观锁定失败作出反应.因为这个类是一个RuntimeException,
 * 因此在一般情况下无需在用户的代码当中去进行声明的"try-catch"去捕捉这个类或者是它的子类的异常类型.
 *
 * 设计这个类的根本原因在于数据访问时, 可能会存在有各种类型的异常, 但是在捕捉这个异常时, 当前的运行时环境当中, 并不一定存在有
 * 这个异常对应的依赖(比如Jar包), 比如MySQL的依赖和SqlServer的依赖, 但是IOC容器在启动时就应该检查出来所有的可能出现的异常,
 * 而不能在运行时出现了链接异常等这些情况.因此在对于不同的数据访问层面的实现时, 都只需要抛出这个异常即可, 不再需要抛出具体的数据访问异常.
 * 如果想要获取具体的数据访问异常, 那么可以从当前的异常的cause当中去寻找你想要的答案.
 *
 * Note: 对于一些持久层的框架来说, 他们不会将真正的[java.sql.SQLException]抛给用户,
 * 会借助这个异常去抛给用户, 因此用户无法捕捉到[java.sql.SQLException]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 */
abstract class DataAccessException(message: String?, cause: Throwable?) : NestedRuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}