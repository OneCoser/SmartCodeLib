package ch.smart.code.adapter

import android.graphics.Color
import androidx.annotation.ColorInt

interface StatusBarAdapter {

    @ColorInt
    fun getBarColor(): Int {
        return Color.WHITE
    }

    fun isLightMode(): Boolean {
        return true
    }
}