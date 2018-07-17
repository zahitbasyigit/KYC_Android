package com.valensas.kyc_android

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.budiyev.android.codescanner.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainView {
    private var mainPresenter: MainPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var SliderAdapter : SliderAdapter = SliderAdapter(this)
        viewPagerIntro.setAdapter(SliderAdapter)
        mainPresenter = MainPresenter()
        mainPresenter?.attach(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter?.detach()
    }
}
