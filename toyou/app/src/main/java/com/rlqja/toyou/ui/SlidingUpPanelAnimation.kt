package com.rlqja.toyou.ui

import android.view.animation.Animation
import android.view.animation.Transformation
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class SlidingUpPanelAnimation(val layout:SlidingUpPanelLayout,val to:Float,val duration:Int):Animation() {
    init{
        setDuration(duration.toLong())
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        val dimension=to*interpolatedTime

        layout.panelHeight=dimension.toInt()
        layout.requestLayout()

    }
}