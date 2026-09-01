package app.gamenative.gamefold.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import app.gamenative.R
import app.gamenative.gamefold.ui.GameFoldNsLoadingContract
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/** Displays the NS loading card while the PC XServer waits for its first app frame. */
class GameFoldGameLoadingOverlayView(context: Context) : View(context) {
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFBFAF7.toInt() }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5E5A57.toInt() }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF787370.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1B1C1D.toInt()
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF68635F.toInt()
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val progressFocusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF6D8F9C.toInt() }
    private val progressActionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFB65454.toInt() }
    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val coverFallback = requireNotNull(
        AppCompatResources.getDrawable(context, R.drawable.pixel_splash_mark),
    ).toBitmap()
    private val coverExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var gameName = "GamePad PC"
    private var statusText = "正在准备运行环境"
    private var gameCover: Bitmap? = null
    private var requestedCover = ""

    init {
        setWillNotDraw(false)
        // The loading card is an overlay above the XServer aperture. Keep the underlying
        // display visible instead of introducing a second opaque full-screen black layer.
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        isClickable = true
    }

    fun setGameName(name: String) {
        if (name.isNotBlank() && gameName != name) {
            gameName = name
            invalidate()
        }
    }

    fun setStatusText(text: String) {
        if (text.isNotBlank() && statusText != text) {
            statusText = text
            invalidate()
        }
    }

    /** Loads the same square cartridge artwork used by the NS loading card. */
    fun setGameCover(uriString: String) {
        if (uriString == requestedCover) return
        requestedCover = uriString
        if (uriString.isBlank()) {
            gameCover = null
            invalidate()
            return
        }
        coverExecutor.execute {
            val decoded = runCatching {
                val uri = Uri.parse(uriString)
                val input = if (uri.scheme.isNullOrBlank() || uri.scheme == "file") {
                    val path = uri.path ?: uriString
                    java.io.File(path).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                input?.use(BitmapFactory::decodeStream)
            }.getOrNull()
            mainHandler.post {
                if (requestedCover == uriString) {
                    gameCover = decoded
                    invalidate()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        mainHandler.removeCallbacksAndMessages(null)
        coverExecutor.shutdownNow()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // fragment_emulation.xml uses a wrap-content 380x144dp card: 16dp padding, a fixed
        // 112dp cover, a 16dp gap and a 220dp text column. Mirror those source dimensions.
        val density = resources.displayMetrics.density
        val scaledDensity = resources.displayMetrics.scaledDensity
        val cardWidth = (GameFoldNsLoadingContract.CardWidthDp * density).coerceAtMost(width * 0.84f)
        val cardHeight = GameFoldNsLoadingContract.CardHeightDp * density
        val left = (width - cardWidth) / 2f
        val top = (height - cardHeight) / 2f
        val shadow = 4f * density
        val faceTrim = 3f * density
        borderPaint.strokeWidth = 2f * density
        canvas.drawRect(left + shadow, top + shadow, left + cardWidth, top + cardHeight, shadowPaint)
        canvas.drawRect(left, top, left + cardWidth - faceTrim, top + cardHeight - faceTrim, cardPaint)
        canvas.drawRect(left, top, left + cardWidth - faceTrim, top + cardHeight - faceTrim, borderPaint)

        val padding = 16f * density
        val coverSize = GameFoldNsLoadingContract.CoverSizeDp * density
        val coverLeft = left + padding
        val coverTop = top + (cardHeight - coverSize) / 2f
        val cover = gameCover ?: coverFallback
        canvas.drawBitmap(cover, null, android.graphics.RectF(coverLeft, coverTop, coverLeft + coverSize, coverTop + coverSize), coverPaint)

        val contentLeft = coverLeft + coverSize + padding
        val contentWidth = cardWidth - (contentLeft - left) - padding
        inkPaint.textSize = 16f * scaledDensity
        mutedPaint.textSize = 12f * scaledDensity
        // Keep the approved first-stage copy/layout; only the leading artwork changes from the
        // generic GameFold mark to the selected game's square cartridge cover.
        val title = gameName.ifBlank { "GamePad PC" }
        val ellipsizedTitle = if (inkPaint.measureText(title) <= contentWidth) title else {
            title.takeLastWhile { true }.let {
                var value = it
                while (value.length > 1 && inkPaint.measureText("$value…") > contentWidth) {
                    value = value.dropLast(1)
                }
                "$value…"
            }
        }
        canvas.drawText(ellipsizedTitle, contentLeft, top + 42f * density, inkPaint)
        canvas.drawText(statusText, contentLeft, top + 70f * density, mutedPaint)

        val segmentTop = top + 88f * density
        // Match NS's direct indeterminate drawable: six segments without a black track/frame.
        repeat(GameFoldNsLoadingContract.SegmentCount) { index ->
            val x = contentLeft + (
                GameFoldNsLoadingContract.SegmentStartDp +
                    index * (GameFoldNsLoadingContract.SegmentWidthDp + GameFoldNsLoadingContract.SegmentGapDp)
                ) * density
            val y = segmentTop + GameFoldNsLoadingContract.SegmentTopDp * density
            canvas.drawRect(
                x,
                y,
                x + GameFoldNsLoadingContract.SegmentWidthDp * density,
                y + GameFoldNsLoadingContract.SegmentHeightDp * density,
                if (index % 2 == 0) progressFocusPaint else progressActionPaint,
            )
        }
    }
}
