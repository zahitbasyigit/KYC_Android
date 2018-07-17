package com.valensas.kyc_android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainView {
    private var mainPresenter: MainPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var SliderAdapter = SliderAdapter(this)
        viewPagerIntro.setAdapter(SliderAdapter)

        mainPresenter = MainPresenter()
        mainPresenter?.attach(this)
        initiateCrossFadeOut()

    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter?.detach()
    }

    private fun initiateCrossFadeIn() {
        crossfadeIn(viewPagerIntro)
        //val intent = Intent(this, IdentityCameraActivity::class.java)
        //startActivity(intent)
    }

    private fun initiateCrossFadeOut() {
        crossfadeOut(titleFirstTextView, false)
        crossfadeOut(titleSecondTextView, false)
        crossfadeOut(welcomeOneImageView, true)
    }


    private fun crossfadeIn(view: View) {
        view.setAlpha(0f)
        view.setVisibility(View.VISIBLE)
        view.animate()
                .alpha(1f)
                .setDuration(1500)
                .setListener(null)
    }

    private fun crossfadeOut(view: View, startCrossFadeIn: Boolean) {

        view.animate()
                .alpha(0f)
                .setDuration(1500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.setVisibility(View.GONE)
                        if (startCrossFadeIn)
                            initiateCrossFadeIn()
                    }
                })
    }
}
