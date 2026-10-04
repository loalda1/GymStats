package org.gymstats.android.ui
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.gymstats.android.R
class TrainingChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var points: List<Pair<String, Double>> = emptyList()
        set(value) { field = value; contentDescription = if (value.isEmpty()) context.getString(R.string.chart_empty) else value.joinToString("; ") { "${it.first}: ${number(it.second)} kg" }; invalidate() }
    var bars = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = resources.displayMetrics.density
        val ink = com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        val brand = com.google.android.material.color.MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)
        paint.textSize = 11 * resources.displayMetrics.scaledDensity; paint.color = ink
        if (points.isEmpty()) { canvas.drawText(context.getString(R.string.chart_empty), 12 * d, height / 2f, paint); return }
        val left = 54 * d; val bottom = height - 32 * d; val top = 16 * d; val right = width - 16 * d
        val maximum = (points.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
        for (i in 0..3) { val y = bottom - (bottom - top) * i / 3f; paint.color = ink; paint.alpha = 40; paint.strokeWidth = d; canvas.drawLine(left, y, right, y, paint); paint.alpha = 255; canvas.drawText(number(maximum * i / 3), 0f, y, paint) }
        val step = (right - left) / points.size.coerceAtLeast(1); path.reset()
        points.forEachIndexed { i, point ->
            val x = left + step * (i + .5f); val y = bottom - ((point.second / maximum) * (bottom - top)).toFloat(); paint.color = brand
            if (bars) canvas.drawRoundRect(x - step * .3f, y, x + step * .3f, bottom, 4 * d, 4 * d, paint)
            else { if (i == 0) path.moveTo(x, y) else path.lineTo(x, y); canvas.drawCircle(x, y, 3 * d, paint) }
            if (points.size <= 7 || i == 0 || i == points.lastIndex) { paint.color = ink; paint.textAlign = Paint.Align.CENTER; canvas.drawText(point.first, x, bottom + 20 * d, paint); paint.textAlign = Paint.Align.LEFT }
        }
        if (!bars) { paint.color = brand; paint.style = Paint.Style.STROKE; paint.strokeWidth = 2 * d; canvas.drawPath(path, paint); paint.style = Paint.Style.FILL }
    }
}
