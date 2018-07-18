package com.valensas.kyc_android.identitysigniture

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentitySignitureDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var paint: Paint? = null
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var previousX: Float? = null
    private var previousY: Float? = null
    private var scale: Float = 1F
    private var width: Int? = null
    private var height: Int? = null

    private var path: Path? = null

    init {
        path = Path()

        paint = Paint()
        paint?.isAntiAlias = true
        paint?.isDither = true
        paint?.color = Color.BLACK
        paint?.style = Paint.Style.STROKE
        paint?.strokeJoin = Paint.Join.ROUND
        paint?.strokeCap = Paint.Cap.BUTT
        paint?.strokeWidth = 12F
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        scale = Math.min(1.0f * w / getWidth(), 1.0f * h / getHeight())
        width = w
        height = h
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(bitmap, 0F, 0F, paint)
        canvas?.drawPath(path, paint)
    }

    fun clearDrawing() {
        setDrawingCacheEnabled(false);
        if (width != null && height != null) {
            onSizeChanged(width!!, height!!, width!!, height!!);
        }
        invalidate();
        setDrawingCacheEnabled(true);
    }

    private fun touchStart(x: Float?, y: Float?) {
        path?.reset();
        path?.moveTo(x!!, y!!)
        previousX = x
        previousY = y
    }

    private fun touchMove(x: Float?, y: Float?) {
        val dx = Math.abs(x!! - previousX!!)
        val dy = Math.abs(y!! - previousY!!)
        if (dx >= 4 || dy >= 4) {
            path?.quadTo(previousX!!, previousY!!, (x + previousX!!) / 2, (y + previousY!!) / 2)
            previousX = x
            previousY = y
        }


    }

    private fun touchUp() {
        if (previousX != null && previousY != null)
            path?.lineTo(previousX!!, previousY!!);

        canvas?.drawPath(path, paint);
        path?.reset();
        previousX = null
        previousY = null
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x
        val y = event?.y

        when (event?.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }

        return true
    }
}