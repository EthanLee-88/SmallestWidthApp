package com.blues.smallestwidthapp.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blues.smallestwidthapp.databinding.ActivityCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : BaseAc() {
    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startButton.setOnClickListener {
            openPreView()
        }
        binding.takePicture.setOnClickListener {
            takePicture()
        }
    }

    private var imageCapture: ImageCapture? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun openPreView() {
//        binding.preView.controller = LifecycleCameraController(this).also {
//            it.bindToLifecycle(this)
//        }
//        binding.preView.controller?.tapToFocusState?.observe(this) {//点击聚焦
//            Log.d(TAG, "openPreView = $it")
//        }
//        binding.preView.controller?.zoomState?.observe(this) {
//            Log.d(
//                TAG, "maxZoomRatio = ${it.maxZoomRatio}" +
//                        "\nminZoomRatio = ${it.minZoomRatio}" +
//                        "\nzoomRatio = ${it.zoomRatio}" +
//                        "\nlinearZoom = ${it.linearZoom}" +
//                        "\n\nmaxZoomRatio = ${binding.preView.controller?.zoomState?.value?.maxZoomRatio}" +
//                        "\nminZoomRatio = ${binding.preView.controller?.zoomState?.value?.minZoomRatio}" +
//                        "\nzoomRatio = ${binding.preView.controller?.zoomState?.value?.zoomRatio}" +
//                        "\nlinearZoom = ${binding.preView.controller?.zoomState?.value?.linearZoom}"
//            )
//        }
////        binding.preView.controller?.setZoomRatio(10F)

        lifecycleScope.launch {
            val provider = withContext(Dispatchers.IO) {
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setJpegQuality(100)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                ProcessCameraProvider.getInstance(this@CameraActivity).get()
            }
            provider.bindToLifecycle(
                this@CameraActivity,
                CameraSelector.DEFAULT_BACK_CAMERA, Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.preView.surfaceProvider)
                }, imageCapture
            )
        }
    }

    private fun takePicture() {

        val ic = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    SimpleDateFormat(
                        "yyyy_MM_dd HH_mm_ss",
                        Locale.CHINA
                    ).format(System.currentTimeMillis())
                )
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                }
            }
        ).build().apply {

        }

//        binding.preView.controller?.imageCaptureTargetSize
//        binding.preView.controller?.takePicture(
//            ic,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//
//                }
//            }
//        )
        imageCapture?.takePicture(
            ic,
            ContextCompat.getMainExecutor(this),
            object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                }

                override fun onError(exception: ImageCaptureException) {

                }
            })
    }
}