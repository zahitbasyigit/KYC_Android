package com.valensas.kyc_android

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class SliderAdapter(context:Context):PagerAdapter() {
    internal var context: Context
    internal lateinit var layoutinflater:LayoutInflater
    public var slide_images = intArrayOf(R.drawable.kyc_tutorial_rocket,  R.drawable.kyc_tutorial_id, R.drawable.kyc_tutorial_face, R.drawable.kyc_tutorial_signature)
    var slide_texts = arrayOf<String>(context.getString(R.string.welcome_1),context.getString(R.string.welcome_2),context.getString(R.string.welcome_3),context.getString(R.string.welcome_4))

    override fun getCount(): Int {
        return slide_images.size    }
    init{
        this.context = context
    }
    override fun isViewFromObject(view: View?, o: Any?): Boolean {
       return view ==  o as ConstraintLayout;
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        layoutinflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
         var view: View = layoutinflater.inflate(R.layout.welcome_2, container, false)

        var slideImageView: ImageView = view.findViewById(R.id.welcomeImageView)
        var slideTextView: TextView = view.findViewById(R.id.welcomeTextView)

        slideImageView.setImageResource(slide_images[position])
        slideTextView.setText(slide_texts[position])

        container?.addView(view)
        return view;
    }

    override fun destroyItem(container: ViewGroup?, position: Int, o: Any?) {
        container?.removeView(o as ConstraintLayout)
    }
}