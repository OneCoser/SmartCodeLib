package ch.smart.code.imageloader

import android.content.Context
import android.util.AttributeSet
import com.facebook.drawee.generic.GenericDraweeHierarchy
import info.liujun.image.LJImageView

class SCImageView : LJImageView {
    
    constructor(context: Context?, hierarchy: GenericDraweeHierarchy?) : super(context, hierarchy)
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    
}
