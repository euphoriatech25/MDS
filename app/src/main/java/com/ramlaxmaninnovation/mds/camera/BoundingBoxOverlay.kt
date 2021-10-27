package com.ramlaxmaninnovation.mds.camera


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView


class BoundingBoxOverlay( context: Context , attributeSet: AttributeSet )
    : SurfaceView( context , attributeSet ) , SurfaceHolder.Callback {

    var areDimsInit = false
    var frameHeight = 0
    var frameWidth = 0
    var faceBoundingBoxes: ArrayList<RectF> = ArrayList()
    var faceNames : ArrayList<String> = ArrayList()

    private var output2OverlayTransform: Matrix = Matrix()

    private val boxPaint = Paint().apply {
        color = Color.parseColor("#4D90caf9")
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        strokeWidth = 2.0f
        textSize = 32f
        color = Color.WHITE
    }



    override fun surfaceCreated(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }


    override fun onDraw(canvas: Canvas?) {
        if (faceBoundingBoxes.size != 0 ) {
            if (!areDimsInit) {
                val viewWidth = width.toFloat()
                val viewHeight = height.toFloat()
                val xFactor: Float = viewWidth / frameWidth.toFloat()
                val yFactor: Float = viewHeight / frameHeight.toFloat()
                output2OverlayTransform.preScale(xFactor, yFactor)
                output2OverlayTransform.postScale(-1f, 1f, viewWidth / 2f, viewHeight / 2f)
                areDimsInit = true
            } else {
                for ( ( boundingBox , name ) in faceBoundingBoxes.zip( faceNames ) ) {
                    output2OverlayTransform.mapRect( boundingBox )
                    Log.e( "APP" , boundingBox.toShortString() )
                    canvas?.drawRoundRect(boundingBox, 16f, 16f, boxPaint)
                    canvas?.drawText(
                        name,
                        boundingBox.centerX(),
                        boundingBox.centerY(),
                        textPaint
                    )
                }
            }
        }
        else {
            Log.e( "APP" , "clar" )
        }
    }
}
