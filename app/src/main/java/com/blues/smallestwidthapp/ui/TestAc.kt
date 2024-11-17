package com.blues.smallestwidthapp.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blues.smallestwidthapp.databinding.AcTestBinding
import java.lang.Thread.UncaughtExceptionHandler
import kotlin.system.exitProcess

class TestAc : AppCompatActivity() {

    companion object {
        private const val TAG = "TestAc8"

        class CauseHandler : UncaughtExceptionHandler {
            var callback: ((p0: Thread, p1: Throwable) -> Unit)? = null

            override fun uncaughtException(p0: Thread, p1: Throwable) {
                Log.d(TAG, "CauseHandler = ${p1.message} - ${p1.toString()}")
                LocalBroadcastManager.getInstance(BaseApp.app).sendBroadcast(Intent().apply {
                    action = TAG
                })
                callback?.invoke(p0, p1)
//                exitProcess(0)
            }

            fun initHandler(): CauseHandler {
                Thread.setDefaultUncaughtExceptionHandler(this)
                return this
            }

            fun unInit() {
                Thread.setDefaultUncaughtExceptionHandler(null)
            }
        }

        fun startMe(context: Context) {
            context.startActivity(Intent(context, TestAc::class.java))
        }
    }

    private var binding: AcTestBinding? = null
    var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcTestBinding.inflate(layoutInflater)
        setContentView(binding?.root)
//        CauseHandler().apply {
//            initHandler().callback = { p0, p1 ->
//                Log.d(TAG, "initHandler().callback = ${recording == null}")
//                recording?.stop()
//                unInit()
//                Toast.makeText(this@TestAc, "闪退", Toast.LENGTH_SHORT).show()
//                throw p1
//            }
//        }
        var run = true
        Handler(Looper.getMainLooper()).post {
            while (run) {
                try {
                    Looper.loop()
                } catch (e: Exception) {
                    e.printStackTrace()
                    recording?.stop()
                    Log.d(TAG, "getMainLooper = ${e.message}")
                    run = false
//                    throw e
                }
            }
        }
        binding?.apply {

        }
        initCamera()
    }

    private fun initCamera() {
        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.HD)
        )
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(this))
            .setQualitySelector(qualitySelector)
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)

        val preview = Preview.Builder().build()
        val viewFinder: PreviewView = binding?.preview!!
        preview.setSurfaceProvider(viewFinder.surfaceProvider)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        try {
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
        val name = "CameraX-recording.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        binding?.left?.setOnClickListener {
            recording = videoCapture.output
                .prepareRecording(this, mediaStoreOutput)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), {

                })
            recording?.resume()
        }
        binding?.right?.setOnClickListener {
            recording?.stop()
        }
        binding?.ex?.setOnClickListener {
//            Thread({
                throw java.lang.NullPointerException("空指针异常")
//            }).start()
//            Log.d(TAG, "binding?.ex")
//            LocalBroadcastManager.getInstance(this)
//                .sendBroadcast(Intent("com.blues.smallestwidthapp")
//                    .apply {
//                        setAction("com.blues.smallestwidthapp")
//                    })
        }
//        binding?.right?.setOnClickListener {
//            sendBro()
//        }
    }

    private fun sendBro() {
        Log.d(TAG, "sendBro")
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(TAG, "onReceive")
//                recording?.stop()
            }
        }, IntentFilter(TAG).apply {
            addAction("com.blues.smallestwidthapp")
        })
    }
}