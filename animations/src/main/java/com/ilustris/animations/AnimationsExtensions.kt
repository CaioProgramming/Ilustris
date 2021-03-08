package com.ilustris.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.silent.ilustriscore.core.utilities.gone
import com.silent.ilustriscore.core.utilities.visible

enum class ViewVisibility {
    GONE, VISIBLE
}

fun View.fadeIn() = makeAnimation(this, R.anim.fade_in, ViewVisibility.VISIBLE)

fun View.fadeOut() = makeAnimation(this, R.anim.fade_in, ViewVisibility.GONE)

fun View.repeatFade() = makeAnimation(this, R.anim.fade_in_repeat, ViewVisibility.VISIBLE)

fun View.bounce() = makeAnimation(this, R.anim.bounce, ViewVisibility.VISIBLE)

fun View.popIn() = makeAnimation(this, R.anim.pop_in, ViewVisibility.VISIBLE)

fun View.popOut() = makeAnimation(this, R.anim.pop_out, ViewVisibility.GONE)

fun View.repeatBounce() = makeAnimation(this, R.anim.bounce_repeat, ViewVisibility.VISIBLE)

fun View.slideInRight() = makeAnimation(this, R.anim.slide_in_right, ViewVisibility.VISIBLE)

fun View.slideOutLeft() = makeAnimation(this, R.anim.slide_out_left, ViewVisibility.GONE)

fun View.flip() = makeAnimation(this, R.anim.flip, ViewVisibility.VISIBLE)

fun View.slideUp() = makeAnimation(this, R.anim.slide_up, ViewVisibility.VISIBLE)

fun View.slideDown() = makeAnimation(this, R.anim.slide_down, ViewVisibility.GONE)

fun View.zoomIn() = makeAnimation(this, R.anim.zoom_in, ViewVisibility.VISIBLE)

fun View.zoomOut() = makeAnimation(this, R.anim.zoom_out, ViewVisibility.GONE)

fun View.rotate() = makeAnimation(this, R.anim.rotate, ViewVisibility.VISIBLE)

fun View.move() = makeAnimation(this, R.anim.move, ViewVisibility.VISIBLE)

fun View.together() = makeAnimation(this, R.anim.together, ViewVisibility.GONE)

private fun makeAnimation(view: View, animation: Int, viewVisibility: ViewVisibility) {
    val anim = AnimationUtils.loadAnimation(view.context, animation)
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
            if (viewVisibility == ViewVisibility.VISIBLE) view.visible()
        }

        override fun onAnimationEnd(p0: Animation?) {
            if (viewVisibility == ViewVisibility.GONE) {
                view.gone()
            }
        }

        override fun onAnimationRepeat(p0: Animation?) {
        }

    })
    view.startAnimation(anim)
}