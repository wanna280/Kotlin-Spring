package com.wanna.framework.transaction.interceptor

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.annotation.BeanFactoryAnnotationUtils
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.core.NamedThreadLocal
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import com.wanna.framework.transaction.PlatformTransactionManager
import com.wanna.framework.transaction.TransactionManager
import com.wanna.framework.transaction.TransactionStatus
import com.wanna.common.logging.LoggerFactory
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap


/**
 * 提供事务的切面的支持的类, 提供了Spring事务要执行的基础模板方法
 *
 * @see invokeWithinTransaction
 * @see TransactionInterceptor
 */
open class TransactionAspectSupport : BeanFactoryAware, InitializingBean {

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionAspectSupport::class.java)

        // 默认事务管理器的Key
        private val DEFAULT_TRANSACTION_MANAGER_KEY = Any()

        // 当前线程正在执行的事务信息
        private val transactionInfoHolder: ThreadLocal<TransactionInfo?> =
            NamedThreadLocal("Current aspect-driven transaction")
    }

    // beanFactory
    private var beanFactory: BeanFactory? = null

    // 事务同步管理器
    private var transactionManager: TransactionManager? = null

    // 事务管理器的beanName
    private var transactionManagerBeanName: String? = null

    // 事务属性源, 提供@Transactional注解的解析
    private var transactionAttributeSource: TransactionAttributeSource? = null

    // 事务同步管理器的缓存
    private var transactionManagerCache = ConcurrentHashMap<Any, TransactionManager>()

    open fun setTransactionManager(transactionManager: TransactionManager) {
        this.transactionManager = transactionManager
    }

    open fun setTransactionAttributeSource(transactionAttributeSource: TransactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource
    }

    open fun getTransactionAttributeSource(): TransactionAttributeSource? = this.transactionAttributeSource

    open fun getTransactionManager(): TransactionManager? = this.transactionManager

    /**
     * 检查相关属性是否为空
     */
    override fun afterPropertiesSet() {
        if (this.beanFactory == null && this.getTransactionManager() == null) {
            throw IllegalStateException("不允许既没有BeanFactory, 又没有设置TransactionManager！")
        }
        if (getTransactionAttributeSource() == null) {
            throw IllegalStateException("TransactionAttributeSource是必须有的, 但是没有设置")
        }
    }

    /**
     * 如果一个方法, 应该以事务的方式去进行运行, 那么应该使用一个环绕的方式去对目标事务方法去进行增强
     *
     * @param method 目标方法
     * @param targetClass 目标类
     * @param callback 执行目标方法的callback
     * @return callback方法的返回值
     */
    protected open fun invokeWithinTransaction(
        method: Method, targetClass: Class<*>?, callback: InvocationCallback
    ): Any? {
        // 使用TransactionAttributeSource, 去解析各类的@Transactional注解, 并封装成为TransactionAttribute
        val txSource = getTransactionAttributeSource()
        val txAttr = txSource?.getTransactionAttribute(method, targetClass)

        // 使用各种策略去决定事务管理器并转换为PlatformTransactionManager
        val transactionManager = determineTransactionManager(txAttr)
        val ptm = asPlatformTransactionManager(transactionManager)

        // 获取方法的id限定名(例如"类名.方法名")
        val joinpointIdentification = methodIdentification(method, targetClass, txAttr)

        // 这里是标准的的getTransaction and commit/rollback的调用的分隔符...

        // 下面将会需要去创建事务并完成事务的提交和回滚操作了...事务的相关信息都会保存到txInfo当中
        val txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification)

        val returnVal: Any?
        try {
            returnVal = callback.proceedWithInvocation()
        } catch (ex: Throwable) {
            // 完成事务, 如果该异常需要回滚的话, 那么需要对该异常去进行回滚
            // 如果该异常不需要回滚的话, 也需要去提交任务...
            completeTransactionAfterThrowing(txInfo, ex)
            throw ex
        } finally {
            // 恢复之前的事务的TransactionInfo(如果之前没有事务方法, 那么恢复为null)
            cleanupTransactionInfo(txInfo)
        }
        // 在方法正常返回时, 应该去提交事务
        commitTransactionAfterReturning(txInfo)
        return returnVal
    }

    /**
     * 使用各种方式去进行尝试, 从而去决定采用哪个TransactionManager
     *
     * @param txAttr 事务属性(可以为null)
     * @return 决策到的TransactionManager(有可能为null)
     * @throws NoSuchBeanDefinitionException 如果从beanFactory当中获取不到TransactionManager
     */
    protected open fun determineTransactionManager(txAttr: TransactionAttribute?): TransactionManager? {
        // 如果txAttr, 或者beanFactory为空, 那么只能直接getTransactionManager了
        if (txAttr == null || beanFactory == null) {
            return getTransactionManager()
        }
        // 如果txAttr&beanFactory都不为空的话, 就可以从beanFactory当中去进行寻找了

        // 1.尝试从@Transactional注解当中去进行获取特殊的TransactionManager
        if (StringUtils.hasText(txAttr.getQualifier())) {
            return determineQualifiedTransactionManager(beanFactory!!, txAttr.getQualifier()!!)

            // 2.如果配置了transactionManagerBeanName
        } else if (StringUtils.hasText(transactionManagerBeanName)) {
            return determineQualifiedTransactionManager(beanFactory!!, transactionManagerBeanName!!)

            // 3.如果还是没有, 那么直接尝试从beanFactory当中去获取TransactionManager...
        } else {
            var defaultTransactionManager = getTransactionManager()
            if (defaultTransactionManager == null) {
                defaultTransactionManager = transactionManagerCache[DEFAULT_TRANSACTION_MANAGER_KEY]
                if (defaultTransactionManager == null) {

                    // getBean... 如果没有TransactionManager, 那么抛出NoSuchBeanDefinitionException...
                    defaultTransactionManager = beanFactory!!.getBean(TransactionManager::class.java)
                    this.transactionManager = defaultTransactionManager
                    this.transactionManagerCache.putIfAbsent(DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager)
                }
            }
            return defaultTransactionManager
        }
    }

    /**
     * 基于Qualifier的方式去决定要使用哪个事务管理器
     *
     * @param beanFactory 需要去寻找TransactionManager的BeanFactory
     * @param qualifier qualifier
     * @return 获取到的事务同步管理器
     * @throws NoSuchBeanDefinitionException 如果无法从beanFactory当中获取到TransactionManager
     */
    protected open fun determineQualifiedTransactionManager(
        beanFactory: BeanFactory,
        qualifier: String
    ): TransactionManager? {
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, TransactionManager::class.java, qualifier)
    }

    /**
     * 将给定的transactionManager对象转换为Platform的TransactionManager, 转换失败, 抛出异常
     *
     * @param transactionManager transactionManager对象
     * @return 转换之后的PlatformTransactionManager
     * @throws IllegalStateException 如果给定的transactionManager为空, 或者它的类型不是PlatformTransactionManager
     */
    protected open fun asPlatformTransactionManager(transactionManager: Any?): PlatformTransactionManager {
        if (transactionManager is PlatformTransactionManager) {
            return transactionManager
        }
        throw IllegalStateException("无法将[${transactionManager?.javaClass?.name}]转换为PlatformTransactionManager")
    }

    /**
     * 如果必要的话, 创建一个事务
     *
     * @param ptm 事务管理器
     * @param txAttr 事务属性
     * @param joinpointIdentification 方法id限定符号
     * @return TransactionInfo(包装了事务的各个组件, 包括ptm,txAttr,joinpointIdentification,transactionStatus)
     */
    protected open fun createTransactionIfNecessary(
        ptm: PlatformTransactionManager, txAttr: TransactionAttribute?, joinpointIdentification: String
    ): TransactionInfo? {
        var txAttrDelegate: TransactionAttribute? = null
        if (txAttr != null) {
            // 创建一层委托的TransactionAttribute, 目的是让name可以返回joinpointIdentification
            txAttrDelegate = object : DelegatingTransactionAttribute(txAttr) {
                override fun getName() = joinpointIdentification
            }
        }

        // 根据事务属性信息, 交给事务管理器去获取事务...
        var status: TransactionStatus? = null
        if (txAttrDelegate != null) {
            status = ptm.getTransaction(txAttrDelegate)
        }

        // 准备txInfo, 包装相关的事务对象到txInfo当中, 并且把txInfo绑定给当前线程
        return prepareTransactionInfo(ptm, txAttrDelegate, joinpointIdentification, status)
    }

    /**
     * 准备TransactionInfo, 将事务(TransactionInfo)绑定给当前线程
     *
     * @param ptm 平台的事务管理器
     * @param txAttr 事务属性
     * @param joinpointIdentification 方法id限定符号
     * @param status 当前最新的事务状态信息
     * @return 包装了ptm/txAtr/joinpointIdentification/status等组件的TransactionInfo
     */
    protected open fun prepareTransactionInfo(
        ptm: PlatformTransactionManager,
        txAttr: TransactionAttribute?,
        joinpointIdentification: String,
        status: TransactionStatus?
    ): TransactionInfo {
        val txInfo = TransactionInfo(ptm, txAttr, joinpointIdentification)
        if (txAttr != null) {
            if (logger.isTraceEnabled) {
                logger.trace("方法[$joinpointIdentification]获取了一个事务")
            }
            txInfo.newTransactionStatus(status)
        } else {
            if (logger.isTraceEnabled) {
                logger.trace("当前方法没必要创建事务, 因为这个方法没有Transactional")
            }
        }
        // 不管我们在这里是否有创建事务, 我们都得将当前的TransactionInfo绑定给当前线程
        // 在这里间接地去维护了一个TransactionInfo的栈信息
        txInfo.bindToThread()
        return txInfo
    }

    /**
     * 在执行过程中抛出异常, 那么应该处理异常并以合适的方式去结束事务
     *
     * 使用事务属性对象去进行检查：
     * * 1.如果该异常需要回滚, 那么对事务去进行回滚操作
     * * 2.如果该异常不需要回滚, 那么对该事务去进行提交操作
     *
     * @param txInfo 事务信息
     * @param ex 异常信息
     */
    protected open fun completeTransactionAfterThrowing(txInfo: TransactionInfo?, ex: Throwable) {
        val transactionStatus = txInfo?.getTransactionStatus()
        if (transactionStatus != null) {
            // 如果该异常需要回滚的话, 回滚事务
            if (txInfo.txAttr != null && txInfo.txAttr.rollbackOn(ex)) {
                txInfo.transactionManager.rollback(transactionStatus)
                // 否则, 提交事务
            } else {
                txInfo.transactionManager.commit(transactionStatus)
            }
        }
    }

    /**
     * 如果必要的话, 需要存储之前的ThreadLocal的事务信息(TransactionInfo)
     *
     * @param txInfo txInfo
     */
    protected open fun cleanupTransactionInfo(txInfo: TransactionInfo?) {
        txInfo?.restoreThreadLocalStatus()
    }

    /**
     * 提交事务; 如果必要的话, 需要去恢复之前已经有的事务的连接
     *
     * @param txInfo txInfo
     */
    protected open fun commitTransactionAfterReturning(txInfo: TransactionInfo?) {
        val transactionStatus = txInfo?.getTransactionStatus()
        if (transactionStatus != null) {
            txInfo.transactionManager.commit(transactionStatus)
        }
    }

    /**
     * 获取方法id限定符
     *
     * @param method method
     * @param targetClass targetClass
     * @param txAttr txAttr
     */
    private fun methodIdentification(method: Method, targetClass: Class<*>?, txAttr: TransactionAttribute?): String {
        return ClassUtils.getQualifiedMethodName(method, targetClass)
    }

    final override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 执行目标方法的回调Callback, 交给子类去进行指定
     */
    @FunctionalInterface
    protected interface InvocationCallback {
        fun proceedWithInvocation(): Any?
    }

    /**
     * TransactionInfo, 维护了事务的相关信息
     *
     * @param joinpointIdentification 方法的id限定名
     * @param txAttr 事务属性
     * @param transactionManager 事务同步管理器
     */
    protected open class TransactionInfo(
        val transactionManager: PlatformTransactionManager,
        val txAttr: TransactionAttribute?,
        val joinpointIdentification: String,
    ) {

        // 当前的最新的事务状态
        private var transactionStatus: TransactionStatus? = null

        // 之前的TransactionInfo
        private var oldTransactionInfo: TransactionInfo? = null

        /**
         * 更新当前的事务的状态信息
         *
         * @param transactionStatus 想要更新的TransactionStatus
         */
        open fun newTransactionStatus(transactionStatus: TransactionStatus?) {
            this.transactionStatus = transactionStatus
        }

        /**
         * 将当前TransactionInfo设置到ThreadLocal当中(绑定给当前线程), 通过oldTransactionInfo配合, 形成了TransactionInfo栈
         */
        open fun bindToThread() {
            this.oldTransactionInfo = transactionInfoHolder.get()  // store old TransactionInfo
            transactionInfoHolder.set(this)  // 将当前的TransactionInfo, 设置到ThreadLocal当中
        }

        open fun getTransactionStatus(): TransactionStatus? = this.transactionStatus

        /**
         * 将oldTransactionInfo保存到ThreadLocal当中
         */
        open fun restoreThreadLocalStatus() {
            transactionInfoHolder.set(oldTransactionInfo)
        }
    }
}