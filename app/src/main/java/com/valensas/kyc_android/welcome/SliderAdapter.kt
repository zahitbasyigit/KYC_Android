package com.valensas.kyc_android.welcome

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.valensas.kyc_android.R

class SliderAdapter(val context: Context) : PagerAdapter() {
    private lateinit var layoutInflater: LayoutInflater

    var slideImages = intArrayOf(R.drawable.kyc_tutorial_rocket, R.drawable.kyc_tutorial_id, R.drawable.kyc_tutorial_face, R.drawable.kyc_tutorial_signature)
    var slideTexts = arrayOf<String>(context.getString(R.string.welcome_1), context.getString(R.string.welcome_2), context.getString(R.string.welcome_3), context.getString(R.string.welcome_4))

    override fun getCount(): Int {
        return slideImages.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o as ConstraintLayout;
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.welcome_2, container, false)

        val slideImageView: ImageView = view.findViewById(R.id.welcomeImageView)
        val slideTextView: TextView = view.findViewById(R.id.welcomeTextView)

        slideImageView.setImageResource(slideImages[position])
        slideTextView.text = slideTexts[position]

        container.addView(view)
        return view;
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        container.removeView(o as ConstraintLayout)
    }
}