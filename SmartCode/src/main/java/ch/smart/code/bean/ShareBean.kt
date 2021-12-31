package ch.smart.code.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * 类描述：分享配置
 */
data class ShareBean(
    val btName: String? = null,
    val desc: String? = null,
    val holdName: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(btName)
        parcel.writeString(desc)
        parcel.writeString(holdName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShareBean> {
        override fun createFromParcel(parcel: Parcel): ShareBean {
            return ShareBean(parcel)
        }

        override fun newArray(size: Int): Array<ShareBean?> {
            return arrayOfNulls(size)
        }
    }
}
