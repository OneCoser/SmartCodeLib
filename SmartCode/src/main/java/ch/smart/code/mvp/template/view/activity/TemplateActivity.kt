package ch.smart.code.mvp.template.view.activity

import android.os.Bundle
import ch.smart.code.mvp.BaseActivity
import ch.smart.code.mvp.template.model.TemplateModel
import ch.smart.code.mvp.template.presenter.TemplatePresenter
import ch.smart.code.mvp.template.view.TemplateView

/**
 * 项目名称：Consignor
 * 类描述：
 * 创建人：chenhao
 * 创建时间：3/11/21 8:42 PM
 * 修改人：chenhao
 * 修改时间：3/11/21 8:42 PM
 * 修改备注：
 * @version
 */
class TemplateActivity : BaseActivity<TemplatePresenter>(), TemplateView {

    override fun createPresenter(): TemplatePresenter? {
        return TemplatePresenter(model = TemplateModel(), rootView = this)
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return 0
    }

    override fun initData(savedInstanceState: Bundle?) {

    }
}