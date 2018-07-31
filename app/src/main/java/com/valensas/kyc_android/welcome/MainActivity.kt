package com.valensas.kyc_android.welcome

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity
import com.valensas.kyc_android.identityresult.IdentityResultActivity
import com.valensas.kyc_android.identitysigniture.IdentitySignitureActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream

private const val REQUEST_CODE = 10

class MainActivity : AppCompatActivity(), MainView, ViewPager.OnPageChangeListener {

    private val fadeInAnimationTime = 10000L
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.RECORD_AUDIO)
    private var mainPresenter: MainPresenter? = null
    private var mDots: Array<TextView>? = null
    private var sliderAdapter: SliderAdapter? = null
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sliderAdapter = SliderAdapter(this)

        mDots = Array(sliderAdapter?.slide_images!!.size) { TextView(this) }

        viewPagerIntro.adapter = sliderAdapter
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
            mDots!![i].text = Html.fromHtml("&#8226")
            mDots!![i].textSize = 35F
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
            val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
            startActivity(intent)
        }


        getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit()


    }

    private fun initiateCrossFadeOut() {

        val fadeoutanim = AnimationUtils.loadAnimation(this, R.anim.fadeout)
        fadeoutanim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(anim: Animation?) {

            }

            override fun onAnimationEnd(anim: Animation?) {
                titleFirstTextView.visibility = View.GONE
                titleSecondTextView.visibility = View.GONE
                welcomeOneImageView.visibility = View.GONE
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })

        titleFirstTextView.startAnimation(fadeoutanim)
        titleSecondTextView.startAnimation(fadeoutanim)
        welcomeOneImageView.startAnimation(fadeoutanim)

        val fadeinanim = AnimationUtils.loadAnimation(this, R.anim.fadein)

        fadeinanim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(anim: Animation?) {

            }

            override fun onAnimationEnd(anim: Animation?) {
                viewPagerIntro.visibility = View.VISIBLE
                dotsLayout.visibility = View.VISIBLE
                subtitleFirstTextView.visibility = View.VISIBLE
                subtitleSecondTextView.visibility = View.VISIBLE
                welcome_next_button.visibility = View.VISIBLE
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })
        fadeinanim.startOffset = 1250L

        viewPagerIntro.startAnimation(fadeinanim)
        dotsLayout.startAnimation(fadeinanim)
        subtitleFirstTextView.startAnimation(fadeinanim)
        subtitleSecondTextView.startAnimation(fadeinanim)
        welcome_next_button.startAnimation(fadeinanim)
    }

    private fun initButtonListeners() {
        welcome_next_button.setOnClickListener {
            if (currentPage < sliderAdapter?.slide_images!!.size - 1) {
                viewPagerIntro.currentItem = currentPage + 1
            }

            if (currentPage == sliderAdapter?.slide_images!!.size - 1) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, 10)
                }
                if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

                    val intent = Intent(this@MainActivity, IdentityCameraActivity::class.java)
                    startActivity(intent)

                }


            }
        }

        welcome_back_button.setOnClickListener {
            if (currentPage < sliderAdapter?.slide_images!!.size) {
                viewPagerIntro.currentItem = currentPage - 1
            }
        }
    }

    private fun initResultActivityWithDummyData() {
        intent = Intent(this, IdentityResultActivity::class.java)
        intent.putExtra("Name", "Zahit")
        intent.putExtra("Surname", "Başyiğit")
        intent.putExtra("TCKN", "36049905174")
        intent.putExtra("Birthday", "12.11.1996")
        startActivity(intent)
    }

    private fun initSignitureActivity() {
        intent = Intent(this, IdentitySignitureActivity::class.java)
        startActivity(intent)
    }

    private fun putImageToIntent(name: String, intent: Intent, bitmap: Bitmap?) {
        if (bitmap != null) {
            val bs = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)
            intent.putExtra(name, bs.toByteArray())
        }
    }
}