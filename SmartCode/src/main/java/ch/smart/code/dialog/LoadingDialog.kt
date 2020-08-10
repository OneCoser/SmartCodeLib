package ch.smart.code.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.AnimationUtils
import ch.smart.code.R
import kotlinx.android.synthetic.main.public_loading_dialog.*

open class LoadingDialog(context: Context) : Dialog(context, R.style.public_dialog_progress) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_loading_dialog)
        loadingAnim.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.public_loading_anim_rotate
            )
        )
    }

}
