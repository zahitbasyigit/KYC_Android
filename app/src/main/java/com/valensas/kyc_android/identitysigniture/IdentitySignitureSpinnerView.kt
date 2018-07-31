package com.valensas.kyc_android.identitysigniture

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.valensas.kyc_android.identitysigniture.IdentitySignitureSpinnerCircle.Direction
import com.valensas.kyc_android.identitysigniture.IdentitySignitureSpinnerCircle.Direction.DOWN
import com.valensas.kyc_android.identitysigniture.IdentitySignitureSpinnerCircle.Direction.UP
import java.util.*

/**
 * Created by Zahit on 31-Jul-18.
 */
class IdentitySignitureSpinnerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var paint: Paint? = null
    private val canvas: Canvas? = null
    private val circles = mutableListOf<IdentitySignitureSpinnerCircle>()
    private var timer = Timer()
    private var width: Int? = null
    private var height: Int? = null

    init {
        paint = Paint()
        paint?.isAntiAlias = true
        paint?.isDither = true
        paint?.color = Color.WHITE
        paint?.style = Paint.Style.FILL

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                update()
            }
        }, 0, 10)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        width = w
        height = h
        initCircles()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //drawCircles(canvas)
    }

    private fun drawCircles(canvas: Canvas?) {
        for (circle in circles) {
            canvas?.drawCircle(circle.x, circle.y, circle.radius, paint)
        }
    }

    var xOffset = 0F

    private fun initCircles() {
        val SMALLEST_RADIUS = getWidth() / 80F
        val MEDIUM_RADIUS = getWidth() / 72F
        val BIG_RADIUS = getWidth() / 64F

        IdentitySignitureSpinnerCircle.COLLISION_RADIUS = BIG_RADIUS

        val MARGIN_X = getWidth() / 20F
        addCirclePair(BIG_RADIUS, MEDIUM_RADIUS, 0.1F, UP)
        xOffset += MARGIN_X
        addCirclePair(MEDIUM_RADIUS, BIG_RADIUS, 0.15F, UP)
        xOffset += MARGIN_X
        addCirclePair(SMALLEST_RADIUS, BIG_RADIUS, 0.45F, UP)
        xOffset += MARGIN_X

        addCirclePair(BIG_RADIUS, SMALLEST_RADIUS, 0.3F, DOWN)
        xOffset += MARGIN_X
        addCirclePair(MEDIUM_RADIUS, MEDIUM_RADIUS, 0.15F, DOWN)
        xOffset += MARGIN_X

        addCirclePair(MEDIUM_RADIUS, MEDIUM_RADIUS, 0.1F, UP)
        xOffset += MARGIN_X
        addCirclePair(SMALLEST_RADIUS, MEDIUM_RADIUS, 0.3F, UP)
        xOffset += MARGIN_X

        addCirclePair(BIG_RADIUS, MEDIUM_RADIUS, 0.45F, DOWN)
        xOffset += MARGIN_X
        addCirclePair(BIG_RADIUS, SMALLEST_RADIUS, 0.2F, DOWN)
        xOffset += MARGIN_X
        addCirclePair(MEDIUM_RADIUS, MEDIUM_RADIUS, 0.1F, DOWN)
        xOffset += MARGIN_X
    }

    private fun addCircle(xOffset: Float, yOffset: Float, radius: Float, direction: Direction) {
        circles.add(IdentitySignitureSpinnerCircle(radius, xOffset + radius, yOffset + radius, direction, getHeight().toFloat()))
    }

    private fun addCirclePair(topRadius: Float, bottomRadius: Float, topPercentage: Float, topDirection: Direction) {
        val bottomDirection = when (topDirection) {
            UP -> DOWN
            DOWN -> UP
        }

        addCircle(xOffset, topPercentage * getHeight(), topRadius, topDirection)
        addCircle(xOffset, (1F - topPercentage) * getHeight() - bottomRadius * 2, bottomRadius, bottomDirection)
    }

    private fun addTranslationAnimation(circle: IdentitySignitureSpinnerCircle, initalDirection: Direction) {
        var valueAnimator = ValueAnimator.ofFloat(circle.y, 0F)
        valueAnimator.duration = 2L
        valueAnimator.addUpdateListener {
            circle.y = valueAnimator.animatedValue as Float
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                valueAnimator = ValueAnimator.ofFloat(0F, getHeight().toFloat())
                valueAnimator.duration = 2L
                valueAnimator.addUpdateListener {

                }


            }
        })

        valueAnimator.start()
    }

    private fun update() {
        for (circle in circles) {
            circle.update()
        }

        (context as Activity).runOnUiThread { invalidate() }
    }

}
