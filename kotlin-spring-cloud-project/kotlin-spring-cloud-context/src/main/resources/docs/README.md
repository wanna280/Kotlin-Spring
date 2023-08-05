# 1. SpringCloud-RefreshScope


## 1.1 RefreshScope是什么? 

在Spring当中一个Bean的作用域包括`singleton`/`prototype`以及自定义Scope三种类型.RefreshScope就是其中的一个自定义的Scope类型.

在Spring当中, 可以使用`@Scope("{scopeName}")`去进行自定义Bean的Scope, 但是简单标注并不生效, 还需要有该Scope的处理器.

对于RefreshScope当中, `@RefreshScope`注解标识它处于RefreshScope作用域内, 而`RefreshScope`这个类则是真正去处理该注解的Scope的处理器.

我们可以像下面这种方式, 将`@RefreshScope`注解标注在配置类上, 当然, 也可以标注在`@Bean`的方法上.

```kotlin
@Configuration
@RefreshScope
class Configuration
```

`RefreshScope`它本身是一个BeanFactoryPostProcessor, 在启动时, 它会将自身注册到`BeanFactory`当中.


配置文件的刷新整体的执行流程如下：

```
RefreshEventListener(接收RefreshEvent事件)
->ContextRefresher(发布EnvironmentChangeEvent事件)
->RefreshScope(发布RefreshScopeRefreshedEvent)事件
```
