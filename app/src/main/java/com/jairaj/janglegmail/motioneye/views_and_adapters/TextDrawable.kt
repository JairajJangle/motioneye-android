package com.jairaj.janglegmail.motioneye.views_and_adapters

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import com.jairaj.janglegmail.motioneye.R

class TextDrawable(context: Context, text: CharSequence) : Drawable() {
    companion object {
        private const val DEFAULT_COLOR = Color.WHITE
        private const val DEFAULT_TEXT_SIZE_IN_DP = 60
    }

    private val mTextBounds = Rect()
    private val mPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val mDrawable: Drawable?

    var text: CharSequence = text
        set(value) {
            field = value
            invalidateSelf()
        }

    init {
        mPaint.color = DEFAULT_COLOR
        mPaint.textAlign = Paint.Align.CENTER
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_TEXT_SIZE_IN_DP.toFloat(), context.resources.displayMetrics
        )
        mPaint.textSize = textSize
        mDrawable = AppCompatResources.getDrawable(context, R.drawable.circle_shortcut)
        mDrawable?.setBounds(0, 0, mDrawable.intrinsicWidth, mDrawable.intrinsicHeight)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        mDrawable?.draw(canvas)
        mPaint.getTextBounds(text.toString(), 0, text.length, mTextBounds)
        val textHeight = mTextBounds.bottom - mTextBounds.top
        canvas.drawText(
            text as String, (bounds.right / 2).toFloat(),
            (bounds.bottom.toFloat() + textHeight + 1) / 2,
            mPaint
        )
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = mPaint.alpha
    override fun getIntrinsicWidth(): Int = mDrawable?.intrinsicWidth ?: 0
    override fun getIntrinsicHeight(): Int = mDrawable?.intrinsicHeight ?: 0

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(filter: ColorFilter?) {
        mPaint.colorFilter = filter
        invalidateSelf()
    }

}