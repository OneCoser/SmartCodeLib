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
import online.daoshang.util.LayoutManagerFactory
import online.daoshang.util.pt

open class ItemAlert(context: Context, listener: ItemAlertClickListener) : BaseAlert(context) {

    interface ItemAlertClickListener {
        fun onClick(alert: ItemAlert, itemIndex: Int, itemTag: Any?)
    }

    private inner class ItemAlertData(val txt: String, @ColorRes val txtColorId: Int, val tag: Any?)
    
    private var loadingV: View? = null
    private var loadingHintV: TextView? = null
    private var alertItemsV: RecyclerView? = null
    private var itemListener: ItemAlertClickListener? = listener
    private val itemList: MutableList<ItemAlertData> = mutableListOf()
    
    override fun getLayoutId(): Int {
        return R.layout.public_alert_item
    }
    
    override fun getGravity(): Int {
        return Gravity.BOTTOM
    }
    
    override fun getWidthRatio(): Double {
        return 1.0
    }
    
    override fun initView(rootView: View) {
        alertItemsV = rootView.findViewById(R.id.alertItems)
        alertItemsV?.layoutManager = LayoutManagerFactory.getLinearLayoutManager(context)
        loadingV = rootView.findViewById(R.id.alert_loading)
        loadingHintV = rootView.findViewById(R.id.alert_loading_hint)
        loadingV?.setOnClickListener {
        }
        hideLoading()
    }

    @JvmOverloads
    fun addItem(
        txt: String,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        tag: Any? = null
    ): ItemAlert {
        itemList.add(ItemAlertData(txt, txtColorId, tag))
        return this
    }

    @JvmOverloads
    fun addItem(
        @StringRes txtId: Int,
        @ColorRes txtColorId: Int = R.color.public_color_333333,
        tag: Any? = null
    ): ItemAlert {
        return addItem(context.getString(txtId), txtColorId, tag)
    }

    override fun show() {
        alertItemsV?.layoutParams?.let {
            it.height = ((if (itemList.size > 5) 5 else itemList.size) * 44f).pt
            alertItemsV?.layoutParams = it
        }
        alertItemsV?.adapter = object : CommonRcvAdapter<ItemAlertData>(itemList) {
            override fun createItem(type: Any): AdapterItem<ItemAlertData> {
                return ItemAlertDataItem()
            }
        }
        super.show()
    }

    fun showLoading(hint: String? = ""): ItemAlert {
        loadingV?.visibility = View.VISIBLE
        loadingHintV?.text = hint
        return this
    }

    fun hideLoading(): ItemAlert {
        loadingV?.visibility = View.GONE
        return this
    }

    private inner class ItemAlertDataItem : LayoutContainerItem<ItemAlertData>() {

        private var itemIndex: Int = 0
        private var nameV: TextView? = null

        override val layoutResId: Int
            get() = R.layout.public_alert_item_data

        override fun setViews() {
            super.setViews()
            nameV = rootView.findViewById(R.id.alert_item_data_name)
            rootView.layoutParams =
                RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 44.pt)
            rootView.click{
                itemListener?.onClick(
                    this@ItemAlert,
                    itemIndex,
                    itemList.getOrNull(itemIndex)?.tag
                )
            }
        }

        override fun handleData(model: ItemAlertData, position: Int) {
            this.itemIndex = position
            nameV?.text = model.txt
            nameV?.setTextColor(ContextCompat.getColor(context, model.txtColorId))
        }
    }
}
