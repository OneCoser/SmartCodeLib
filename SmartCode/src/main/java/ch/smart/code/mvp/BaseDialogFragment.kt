package ch.smart.code.mvp

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import ch.smart.code.R
import ch.smart.code.mvp.lifecycle.FragmentLifecycleable
import com.trello.rxlifecycle3.android.FragmentEvent
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber

/**
 * 类描述：DialogFragment基类
 */
abstract class BaseDialogFragment<P : IPresenter> : DialogFragment(), IFragment,
    FragmentLifecycleable, PageNameable {

    private val mLifecycleSubject = BehaviorSubject.create<FragmentEvent>()
    protected open var bottomPopUp = false

    @JvmField
    protected var presenter: P? = this.createPresenter()

    abstract fun createPresenter(): P?

    override fun provideLifecycleSubject(): Subject<FragmentEvent> {
        return mLifecycleSubject
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (bottomPopUp) {
            val window = dialog?.window
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setWindowAnimations(R.style.public_dialog_anim_bottom)
            dialog?.setCancelable(true)
            dialog?.setCanceledOnTouchOutside(true)
            val params = window?.attributes
            params?.gravity = Gravity.BOTTOM
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            window?.attributes = params
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy() // 释放资源
        presenter = null
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return try {
            super.show(transaction, tag)
        } catch (e: Exception) {
            Timber.e(e)
            -1
        }
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        try {
            super.showNow(manager, tag)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getPageName(): String {
        return this.javaClass.simpleName
    }

    override fun getPagePath(): String {
        val act = activity
        return if (act is PageNameable) "${act.getPageName()}-${getPageName()}" else getPageName()
    }
}

/**
 * 防止崩溃的showDialogFragment
 */
fun DialogFragment.safeShow(manager: FragmentManager, tag: String) {
    try {
        this.show(manager, tag)
    } catch (e: Exception) {
        Timber.e(e)
    }
}
