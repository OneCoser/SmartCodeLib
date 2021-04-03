package ch.smart.code.network

/**
 * 类描述：一个空的网络请求订阅者
 * 只需要调用接口，而不需要关心接口返回数据时可以使用
 */
open class EmptyObserver constructor(showMsg: Boolean = false) : HttpObserver<Any?>(showMsg) {
    override fun onNext(o: Any) {}
}
