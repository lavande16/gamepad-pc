package app.gamenative.gamefold.gyro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import java.util.Locale
import kotlin.math.min

internal class GyroSteeringDiagnosticsView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val panel = RectF()
    private val modeButton = RectF()
    private val recenterButton = RectF()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xC2181A1F.toInt()
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xD93C4048.toInt()
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
    }
    private var output = GyroSteeringOutput(false, 0f, 0f)
    private var pressedTarget: Target? = null
    private var onModeChanged: (GyroSteeringMode) -> Unit = {}
    private var onRecenter: () -> Unit = {}

    fun setActions(
        onModeChanged: (GyroSteeringMode) -> Unit,
        onRecenter: () -> Unit,
    ) {
        this.onModeChanged = onModeChanged
        this.onRecenter = onRecenter
    }

    fun update(newOutput: GyroSteeringOutput) {
        output = newOutput
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateGeometry()
        val radius = 12f * density
        canvas.drawRoundRect(panel, radius, radius, backgroundPaint)
        canvas.drawRoundRect(modeButton, radius * 0.65f, radius * 0.65f, buttonPaint)
        canvas.drawRoundRect(recenterButton, radius * 0.65f, radius * 0.65f, buttonPaint)

        textPaint.textSize = 13f * density
        val status = if (output.calibrated) "READY" else "HOLD STILL"
        canvas.drawText(
            String.format(
                Locale.US,
                "GYRO $status  AXIS:${output.activeAxis}  W:%+.1f° Y:%+.1f° X:%+.2f",
                output.wheelDegrees,
                output.yawDegrees,
                output.stickX,
            ),
            panel.left + 14f * density,
            panel.top + 23f * density,
            textPaint,
        )

        textPaint.textSize = 14f * density
        canvas.drawText(
            "模式 ${modeLabel(output.mode)}",
            modeButton.left + 13f * density,
            modeButton.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint,
        )
        canvas.drawText(
            "回正",
            recenterButton.left + 22f * density,
            recenterButton.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint,
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        updateGeometry()
        val target = targetAt(event.x, event.y)
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (target == null) return false
                pressedTarget = target
                true
            }
            MotionEvent.ACTION_UP -> {
                val pressed = pressedTarget
                pressedTarget = null
                if (pressed != null && pressed == target) {
                    when (pressed) {
                        Target.MODE -> onModeChanged(output.mode.next())
                        Target.RECENTER -> onRecenter()
                    }
                    performClick()
                }
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedTarget = null
                true
            }
            else -> pressedTarget != null
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateGeometry() {
        val panelWidth = min(width - 24f * density, 680f * density).coerceAtLeast(0f)
        val panelHeight = 82f * density
        val left = (width - panelWidth) / 2f
        val top = 8f * density
        panel.set(left, top, left + panelWidth, top + panelHeight)
        val buttonTop = top + 38f * density
        modeButton.set(left + 12f * density, buttonTop, left + 144f * density, top + 72f * density)
        recenterButton.set(left + 154f * density, buttonTop, left + 254f * density, top + 72f * density)
    }

    private fun targetAt(x: Float, y: Float): Target? = when {
        modeButton.contains(x, y) -> Target.MODE
        recenterButton.contains(x, y) -> Target.RECENTER
        else -> null
    }

    private fun modeLabel(mode: GyroSteeringMode): String = when (mode) {
        GyroSteeringMode.AUTO -> "自动"
        GyroSteeringMode.WHEEL -> "方向盘"
        GyroSteeringMode.YAW -> "侧倾"
    }

    private enum class Target {
        MODE,
        RECENTER,
    }
}
