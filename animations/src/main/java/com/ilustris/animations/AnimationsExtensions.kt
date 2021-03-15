package com.ilustris.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils



fun View.fadeIn() = makeAnimation(this, R.anim.fade_in, View.VISIBLE)

fun View.fadeOut() = makeAnimation(this, R.anim.fade_out, View.GONE)

fun View.repeatFade() = makeAnimation(this, R.anim.fade_in_repeat, View.VISIBLE)

fun View.bounce() = makeAnimation(this, R.anim.bounce, View.VISIBLE)

fun View.popIn() = makeAnimation(this, R.anim.pop_in, View.VISIBLE)

fun View.popOut() = makeAnimation(this, R.anim.pop_out, View.GONE)

fun View.repeatBounce() = makeAnimation(this, R.anim.bounce_repeat, View.VISIBLE)

fun View.slideInRight() = makeAnimation(this, R.anim.slide_in_right, View.VISIBLE)

fun View.slideOutLeft() = makeAnimation(this, R.anim.slide_out_left, View.GONE)

fun View.flip() = makeAnimation(this, R.anim.flip, View.VISIBLE)

fun View.slideUp() = makeAnimation(this, R.anim.slide_up, View.GONE)

fun View.slideInBottom() = makeAnimation(this, R.anim.slide_in_bottom, View.VISIBLE)

fun View.slideDown() = makeAnimation(this, R.anim.slide_down, View.GONE)

fun View.zoomIn() = makeAnimation(this, R.anim.zoom_in, View.VISIBLE)

fun View.zoomOut() = makeAnimation(this, R.anim.zoom_out, View.GONE)

fun View.rotate() = makeAnimation(this, R.anim.rotate, View.VISIBLE)

fun View.move() = makeAnimation(this, R.anim.move, View.VISIBLE)

fun View.together() = makeAnimation(this, R.anim.together, View.GONE)

private fun makeAnimation(view: View, animation: Int, viewVisibility: Int) {
    val anim = AnimationUtils.loadAnimation(view.context, animation)
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
            if (viewVisibility == View.VISIBLE) view.visibility = viewVisibility
        }

        override fun onAnimationEnd(p0: Animation?) {
            if (viewVisibility == View.GONE) {
                view.visibility = viewVisibility
            }
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }

    })
    view.startAnimation(anim)
}