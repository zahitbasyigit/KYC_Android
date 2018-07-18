package com.valensas.kyc_android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.budiyev.android.codescanner.*
import com.valensas.kyc_android.identitysigniture.IdentitySignitureActivity
import com.valensas.kyc_android.identityviacamera.IdentityCameraActivity
import com.valensas.kyc_android.qrreader.QRReaderActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

class MainActivity() : AppCompatActivity(), MainView, ViewPager.OnPageChangeListener {

    private val fadeInAnimationTime = 1500L

    private var mainPresenter: MainPresenter? = null
    private var mDots: Array<TextView>? = null
    var sliderAdapter: SliderAdapter? = null
    var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sliderAdapter = SliderAdapter(this)

        mDots = Array<TextView>(sliderAdapter?.slide_images!!.size) { TextView(this) }

        viewPagerIntro.setAdapter(sliderAdapter)
        mainPresenter = MainPresenter()
        mainPresenter?.attach(this)

        addDotsIndicator(0)
        viewPagerIntro.addOnPageChangeListener(this)

        welcome_next_button.setOnClickListener {
            if (currentPage!! < sliderAdapter?.slide_images!!.size - 1) {
                viewPagerIntro.setCurrentItem(currentPage!! + 1)
            }

            if (currentPage == sliderAdapter?.slide_images!!.size - 1) {
                val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
                startActivity(intent)
            }
        }

        welcome_back_button.setOnClickListener {
            if (currentPage!! < sliderAdapter?.slide_images!!.size) {
                viewPagerIntro.setCurrentItem(currentPage!! - 1)
            }
        }

        initiateCrossFadeOut()

    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter?.detach()
    }

    private fun addDotsIndicator(position: Int) {

        dotsLayout.removeAllViews()
        for (i in 0 until sliderAdapter?.slide_images!!.size) {

            mDots!![i] = TextView(this)
            mDots!![i].setText(Html.fromHtml("&#8226"))
            mDots!![i].setTextSize(35F)
            mDots!![i].setTextColor(getResources().getColor(R.color.transparentwhite))

            dotsLayout.addView(mDots!![i])

        }

        if (mDots!!.size > 0) mDots!![position].setTextColor(getResources().getColor(R.color.white))
    }

    override fun onPageScrolled(i: Int, v: Float, j: Int) {

    }

    override fun onPageSelected(i: Int) {
        addDotsIndicator(i)
        currentPage = i

        when (currentPage) {
            0 -> {
                welcome_back_button.setEnabled(false)
                welcome_back_button.setVisibility(View.INVISIBLE)
                welcome_next_button.setText(R.string.welcome_next_button_text)
            }
            mDots!!.lastIndex -> {
                welcome_back_button.setEnabled(true)
                welcome_back_button.setVisibility(View.VISIBLE)
                welcome_next_button.setText(R.string.welcome_start_button_text)
                welcome_back_button.setText(R.string.welcome_back_button_text)

            }
            else -> {
                welcome_back_button.setEnabled(true)
                welcome_next_button.setEnabled(true)
                welcome_next_button.setText(R.string.welcome_next_button_text)
                welcome_back_button.setVisibility(View.VISIBLE)
                welcome_back_button.setText(R.string.welcome_back_button_text)
            }
        }

    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun initiateCrossFadeIn() {
        crossfadeIn(viewPagerIntro)
        crossfadeIn(dotsLayout)
        crossfadeIn(subtitleFirstTextView)
        crossfadeIn(subtitleSecondTextView)
        crossfadeIn(welcome_next_button)
        val intent = Intent(this, IdentitySignitureActivity::class.java)
        startActivity(intent)
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
                .setDuration(fadeInAnimationTime)
                .setListener(null)
    }

    private fun crossfadeOut(view: View, startCrossFadeIn: Boolean) {

        view.animate()
                .alpha(0f)
                .setDuration(fadeInAnimationTime)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.setVisibility(View.GONE)
                        if (startCrossFadeIn)
                            initiateCrossFadeIn()
                    }
                })
    }
}