package ch.smart.code.mvp

/**
 * 类描述：
 */
interface IModel {

    /**
     * 在框架中 [BasePresenter.onDestroy] 时会默认调用 [IModel.onDestroy]
     */
    fun onDestroy()
}