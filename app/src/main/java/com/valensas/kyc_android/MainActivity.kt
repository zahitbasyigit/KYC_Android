package com.valensas.kyc_android

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.TextView
import com.valensas.kyc_android.identityviacamera.IdentityCameraActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_CODE = 10

class MainActivity() : AppCompatActivity(), MainView, ViewPager.OnPageChangeListener {

    private val fadeInAnimationTime = 1500L
    private val permissions = arrayOf(Manifest.permission.CAMERA)
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
        initButtonListeners()
        initiateCrossFadeOut()

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
                startActivity(intent)
            }
        }
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
        val isFirstRun = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            crossfadeIn(viewPagerIntro)
            crossfadeIn(dotsLayout)
            crossfadeIn(subtitleFirstTextView)
            crossfadeIn(subtitleSecondTextView)
            crossfadeIn(welcome_next_button)
        } else {
            val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
            startActivity(intent)
        }


        getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit()


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

    private fun initButtonListeners() {
        welcome_next_button.setOnClickListener {
            if (currentPage!! < sliderAdapter?.slide_images!!.size - 1) {
                viewPagerIntro.setCurrentItem(currentPage!! + 1)
            }

            if (currentPage == sliderAdapter?.slide_images!!.size - 1) {


                if (ContextCompat.checkSelfPermission(this, permissions.get(0)) == PackageManager.PERMISSION_GRANTED) {

                    val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
                    startActivity(intent)

                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, 10)
                    }
                }


            }
        }

        welcome_back_button.setOnClickListener {
            if (currentPage!! < sliderAdapter?.slide_images!!.size) {
                viewPagerIntro.setCurrentItem(currentPage!! - 1)
            }
        }
    }
}