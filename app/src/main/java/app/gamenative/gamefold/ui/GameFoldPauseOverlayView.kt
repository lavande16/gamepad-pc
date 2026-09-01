package app.gamenative.gamefold.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import android.graphics.Bitmap

/** NS-style pause affordance constrained to the GameFold display aperture. */
class GameFoldPauseOverlayView(context: Context) : View(context) {
    private val playBitmap: Bitmap = rasterizeDrawable(
        requireNotNull(AppCompatResources.getDrawable(context, app.gamenative.R.drawable.pixel_icon_play)),
        width = 24,
        height = 24,
    )
    private val buttonBitmap: Bitmap = rasterizeDrawable(
        // Rasterizing a selector freezes whichever state happened to be resolved at creation
        // time. Use the explicit NS normal action face so the pause affordance stays deep red.
        requireNotNull(AppCompatResources.getDrawable(context, app.gamenative.R.drawable.pixel_action_button_normal)),
        width = 72,
        height = 72,
    )
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var pausedFrame: Bitmap? = null
    private var resumeAction: (() -> Unit)? = null
    private var buttonBounds = RectF()

    private fun rasterizeDrawable(drawable: android.graphics.drawable.Drawable, width: Int, height: Int): Bitmap {
        // Pixel selector/layer-list drawables intentionally have no intrinsic bounds.  Calling
        // Drawable.toBitmap() therefore produces a 0x0 bitmap and crashes as soon as the game
        // surface creates the pause overlay.  Give the drawable the same explicit aperture size
        // used by the renderer before rasterizing it.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    init {
        isClickable = true
        setWillNotDraw(false)
    }

    fun setOnResumeAction(action: (() -> Unit)?) {
        resumeAction = action
    }

    fun setPausedFrame(frame: Bitmap?) {
        if (pausedFrame !== frame) pausedFrame?.recycle()
        pausedFrame = frame
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pausedFrame?.let { frame ->
            val scale = minOf(width.toFloat() / frame.width, height.toFloat() / frame.height)
            val frameWidth = frame.width * scale
            val frameHeight = frame.height * scale
            val frameLeft = (width - frameWidth) / 2f
            val frameTop = (height - frameHeight) / 2f
            canvas.drawBitmap(
                frame,
                null,
                RectF(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight),
                bitmapPaint,
            )
        }
        // NS keeps a captured paused frame and controller visible; only a compact 72dp pixel play
        // affordance is drawn above it.
        val size = (72f * resources.displayMetrics.density).coerceAtLeast(72f)
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        buttonBounds = RectF(left, top, left + size, top + size)
        canvas.drawBitmap(buttonBitmap, null, buttonBounds, bitmapPaint)
        val iconSize = size * 0.42f
        val iconLeft = left + (size - iconSize) / 2f
        val iconTop = top + (size - iconSize) / 2f
        canvas.drawBitmap(playBitmap, null, RectF(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize), bitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP && buttonBounds.contains(event.x, event.y)) {
            performClick()
            resumeAction?.invoke()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        pausedFrame?.recycle()
        pausedFrame = null
        super.onDetachedFromWindow()
    }
}
