package com.ilustris.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.silent.ilustriscore.core.utilities.gone
import com.silent.ilustriscore.core.utilities.visible

fun View.fadeIn() {
    visible()
    val fadein = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    startAnimation(fadein)
}

fun View.fadeOut() {
    val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            gone()
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }

    })
    startAnimation(fadeOut)
}

fun View.repeatFade() {
    val fadeRepeat = AnimationUtils.loadAnimation(context, R.anim.fade_in_repeat)
    startAnimation(fadeRepeat)
}

fun View.bounce() {
    val bounce = AnimationUtils.loadAnimation(context, R.anim.bounce)
    startAnimation(bounce)
}

fun View.popIn() {
    visible()
    val popIn = AnimationUtils.loadAnimation(context, R.anim.pop_in)
    startAnimation(popIn)
}

fun View.popOut() {
    val popOut = AnimationUtils.loadAnimation(context, R.anim.pop_out)
    popOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
        }

        override fun onAnimationEnd(p0: Animation?) {
            gone()
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }

    })
    startAnimation(popOut)
}

fun View.repeatBounce() {
    val bounceRepeat = AnimationUtils.loadAnimation(context, R.anim.bounce_repeat)
    startAnimation(bounceRepeat)
}

fun View.slideInBottom() {
    val slideInBottom = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom)
    startAnimation(slideInBottom)
}

fun View.slideOutBottom() {
    val slideInBottom = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
    startAnimation(slideInBottom)
}

fun View.slideOutLeft() {
    val slideInBottom = AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
    startAnimation(slideInBottom)
}