package com.eddp.nodontcallme.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout

class AnimatedHowToUse : RelativeLayout {
    private lateinit var v: View

    private var animDuration: Long = 0

    // Initialisation
    constructor(context: Context) : super(context) {
        initAnims()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAnims()
    }

   private fun initAnims() {
        animDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
   }

    fun setView(view: View) { this.v = view }

    // Functions
    fun toggle(open: Boolean) {
        val anim: TranslateAnimation

        if (open) {
            anim = TranslateAnimation(0f, 0f, -this.height.toFloat(), 0f)
            anim.setAnimationListener(AnimatedHowToUseOpenListener())
        } else {
            anim = TranslateAnimation(0f, 0f, 0f, -this.height.toFloat())
            anim.setAnimationListener(AnimatedHowToUseCloseListener())
        }

        anim.duration = animDuration
        anim.interpolator = AccelerateInterpolator()

        startAnimation(anim)
    }

    // Listeners
    inner class AnimatedHowToUseOpenListener : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) { v.visibility = View.VISIBLE }

        override fun onAnimationEnd(animation: Animation?) { }

        override fun onAnimationRepeat(animation: Animation?) { }
    }

    inner class AnimatedHowToUseCloseListener : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) { }

        override fun onAnimationEnd(animation: Animation?) { v.visibility = View.GONE }

        override fun onAnimationRepeat(animation: Animation?) { }
    }
}