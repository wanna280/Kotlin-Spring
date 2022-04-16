# 1. BeanDefinition的分类

* 1.通过注解导入得到的BeanDefinition为AnnotatedGenericBeanDefinition
* 2.注册默认的组件时，使用的是RootBeanDefinition

所有的BeanDefinition在最终都会使用merge成为一个RootBeanDefinition

# 2. 对于BeanNameGenerator的设置

* 1.如果在ConfigurationClassPostProcessor中进行配置了`setBeanNameGenerator`，那么就采用这个作为BeanNameGenerator
* 2.如果你没有配置BeanDefinitionGenerator，那么从SingleBeanRegistry中尝试去进行获取BeanDefinitionGenerator并进行使用。
* 3.如果SingleBeanRegistry中没有获取到，那么就使用默认的(普通的Bean使用AnnotationBeanNameGenerator生成，Import的Bean采用FullyQualifiedAnnotationBeanNameGenerator生成)

# 3. AutowireCandidateResolver

* 1.父接口--AutowireCandidateResolver
* 2.简单实现--SimpleAutowireCandidateResolver
* 3.支持泛型--GenericTypeAwareAutowireCandidateResolver
* 4.支持Qualifier相关注解--QualifierAnnotationAutowireCandidateResolver
* 5.支持Context相关注解(例如Lazy)--ContextAnnotationAutowireCandidateResolver