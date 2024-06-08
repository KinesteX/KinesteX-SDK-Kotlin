package com.kinestex.kotlin_sdk
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.ActionBar
import android.view.View

object AnimationUtils {
    fun collapse(view: View, duration: Long) {
        val initialHeight = view.height
        val animator = ValueAnimator.ofInt(initialHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        animator.duration = duration
        animator.start()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                view.visibility = View.GONE
                val layoutParams = view.layoutParams
                layoutParams.height = ActionBar.LayoutParams.WRAP_CONTENT
                view.layoutParams = layoutParams
            }
        })
    }

    fun expand(view: View, duration: Long) {
        view.measure(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight
        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        animator.duration = duration
        animator.start()
    }
}