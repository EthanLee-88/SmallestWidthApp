package com.blues.smallestwidthapp.ui.video.glutilsOld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class PlayButton : View {

    private var outPaint: Paint? = null
    private var innerPaint: Paint? = null

    @ColorInt
    private var mOuterBorderColor: Int = 0

    @ColorInt
    private var innerCorePaint: Int = 0

    private var mStepBorderWidth = 8f

    private var recording: Boolean = false

    private val rectF = RectF()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, style: Int) : super(
        context,
        attributeSet,
        style
    ) {
        init()
    }

    fun setRecording(record: Boolean) {
        this.recording = record
        invalidate()
    }

    fun getRecording(): Boolean {
        return this.recording
    }

    private fun init() {
        mOuterBorderColor = Color.WHITE
        outPaint = Paint()
        outPaint?.color = mOuterBorderColor //画笔颜色
        outPaint?.isAntiAlias = true // 抗锯齿
        outPaint?.isDither = true // 防抖动
        outPaint?.style = Paint.Style.STROKE //填充风格
        outPaint?.strokeCap = Paint.Cap.ROUND // 设置线帽样式:圆形线帽
        outPaint?.strokeWidth = mStepBorderWidth // 宽度

        innerCorePaint = Color.RED
        innerPaint = Paint()
        innerPaint?.color = innerCorePaint //画笔颜色
        innerPaint?.isAntiAlias = true // 抗锯齿
        innerPaint?.isDither = true // 防抖动
        innerPaint?.style = Paint.Style.FILL //填充风格
        innerPaint?.strokeCap = Paint.Cap.ROUND // 设置线帽样式:圆形线帽

    }

    override fun onDraw(canvas: Canvas?) {
        rectF.set(
            mStepBorderWidth / 2,
            mStepBorderWidth / 2,
            width - mStepBorderWidth / 2,
            height - mStepBorderWidth / 2
        )
        outPaint?.let {
            canvas?.drawArc(rectF, 0f, 360f, false, it)
        }
        rectF.set(
            (width / 3).toFloat(),
            (height / 3).toFloat(),
            (width * 2 / 3).toFloat(),
            (height * 2 / 3).toFloat()
        )
        innerPaint?.let {
            if (this.recording) {
                canvas?.drawRect(rectF, it)
            } else {
                canvas?.drawCircle(
                    (width / 2).toFloat(),
                    (height / 2).toFloat(), width / 2 - 3 * mStepBorderWidth, it
                )
            }
        }
    }
}