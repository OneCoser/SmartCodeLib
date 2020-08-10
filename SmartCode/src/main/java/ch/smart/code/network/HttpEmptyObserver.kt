package ch.smart.code.network

class HttpEmptyObserver(showMsg: Boolean = false) : HttpObserver<Any>(showMsg) {
    override fun onNext(o: Any) {}
}