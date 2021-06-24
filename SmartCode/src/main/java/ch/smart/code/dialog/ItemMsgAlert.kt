package ch.smart.code.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ch.smart.code.R
import ch.smart.code.adapter.CommonRcvAdapter
import ch.smart.code.adapter.item.AdapterItem
import ch.smart.code.adapter.item.LayoutContainerItem
import ch.smart.code.util.click
import ch.smart.code.util.LayoutManagerFactory
import ch.smart.code.util.isNotNullOrBlank
import ch.smart.code.util.pt

open class ItemMsgAlert(
    context: Context,
    private val showCount: Int = 5,
    private val itemHeight: Int = 44.pt
) : BaseAlert(context) {

    interface ItemMsgAlertClickListener {
        fun onClick(alert: ItemMsgAlert, itemIndex: Int, itemTag: Any?)
    }

    private inner class ItemMsgAlertData(
        val txt: String,
        @ColorRes val txtColorId: Int,
        val tag: Any?
    )

    private var loadingV: View? = null
    private var loadingHintV: TextView? = null
    private var titleV: TextView? = null
    private var contentV: TextView? = null
    private var contentHintV: TextView? = null
    private var alertItemsV: RecyclerView? = null
    private var itemListener: ItemMsgAlertClickListener? = null
    private val itemList: MutableList<ItemMsgAlertData> = mutableListOf()

    override fun getLayoutId(): Int {
        return R.layout.public_alert_item_msg
    }

    override fun getGravity(): Int {
        return Gravity.CENTER
    }

    override fun initView(rootView: View) {
        titleV = rootView.findViewById(R.id.alert_title)
        contentV = rootView.findViewById(R.id.alert_msg)
        contentHintV = rootView.findViewById(R.id.alert_msg_hint)
        alertItemsV = rootView.findViewById(R.id.alertItems)
        alertItemsV?.layoutManager = LayoutManagerFactory.getLinearLayoutManager(context)
        loadingV = rootView.findViewById(R.id.alert_loading)
        loadingHintV = rootView.findViewById(R.id.alert_loading_hint)
        loadingV?.setOnClickListener {

        }
        contentHintV?.visibility = View.GONE
        hideLoading()
    }

    fun setListener(listener: ItemMsgAlertClickListener): ItemMsgAlert {
        this.itemListener = listener
        return this
    }

    @JvmOverloads
    fun setTitle(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333
    ): ItemMsgAlert {
        titleV?.let {
            it.text = txt
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            it.visibility = if (txt.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        return this
    }

    @JvmOverloads
    fun setTitle(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333
    ): ItemMsgAlert {
        return setTitle(context.getString(txtId), txtColorId)
    }

    @JvmOverloads
    fun setMsg(msg: String, @ColorRes txtColorId: Int = R.color.public_color_333333): ItemMsgAlert {
        contentV?.let {
            it.text = msg
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
        }
        return this
    }

    @JvmOverloads
    fun setMsg(
        @StringRes msgId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333
    ): ItemMsgAlert {
        return setMsg(context.getString(msgId), txtColorId)
    }

    @JvmOverloads
    fun setMsgHint(
        hint: String,
        @ColorRes txtColorId: Int = R.color.public_color_999999
    ): ItemMsgAlert {
        contentHintV?.let {
            it.text = hint
            it.setTextColor(ContextCompat.getColor(context, txtColorId))
            it.visibility = if (hint.isNotNullOrBlank()) View.VISIBLE else View.GONE
        }
        return this
    }

    @JvmOverloads
    fun setMsgHint(
        @StringRes hintId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_999999
    ): ItemMsgAlert {
        return setMsgHint(context.getString(hintId), txtColorId)
    }

    @JvmOverloads
    fun addItem(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        tag: Any? = null
    ): ItemMsgAlert {
        itemList.add(ItemMsgAlertData(txt, txtColorId, tag))
        return this
    }

    @JvmOverloads
    fun addItem(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        tag: Any? = null
    ): ItemMsgAlert {
        return addItem(context.getString(txtId), txtColorId, tag)
    }

    override fun show() {
        alertItemsV?.layoutParams?.let {
            it.height = (if (itemList.size > showCount) showCount else itemList.size) * itemHeight
            alertItemsV?.layoutParams = it
        }
        alertItemsV?.adapter = object : CommonRcvAdapter<ItemMsgAlertData>(itemList) {
            override fun createItem(type: Any): AdapterItem<ItemMsgAlertData> {
                return ItemMsgAlertDataItem()
            }
        }
        super.show()
    }

    fun showLoading(hint: String? = ""): ItemMsgAlert {
        loadingV?.visibility = View.VISIBLE
        loadingHintV?.text = hint
        return this
    }

    fun hideLoading(): ItemMsgAlert {
        loadingV?.visibility = View.GONE
        return this
    }

    private inner class ItemMsgAlertDataItem : LayoutContainerItem<ItemMsgAlertData>() {

        private var itemIndex: Int = 0
        private var nameV: TextView? = null

        override val layoutResId: Int
            get() = R.layout.public_alert_item_data

        override fun setViews() {
            super.setViews()
            nameV = rootView.findViewById(R.id.alert_item_data_name)
            rootView.layoutParams =
                RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, itemHeight)
            rootView.click {
                itemListener?.onClick(
                    this@ItemMsgAlert,
                    itemIndex,
                    itemList.getOrNull(itemIndex)?.tag
                )
            }
        }

        override fun handleData(model: ItemMsgAlertData, position: Int) {
            this.itemIndex = position
            nameV?.text = model.txt
            nameV?.setTextColor(ContextCompat.getColor(context, model.txtColorId))
        }
    }

}