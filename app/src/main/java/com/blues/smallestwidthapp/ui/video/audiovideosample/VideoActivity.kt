package com.blues.smallestwidthapp.ui.video.audiovideosample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blues.smallestwidthapp.databinding.ActivityVideoBinding
import com.blues.smallestwidthapp.ui.video.encoder.MediaAudioEncoder
import com.blues.smallestwidthapp.ui.video.encoder.MediaEncoder
import com.blues.smallestwidthapp.ui.video.encoder.MediaMuxerWrapper
import com.blues.smallestwidthapp.ui.video.encoder.MediaVideoEncoder
import com.blues.smallestwidthapp.ui.video.glutilsOld.RomUtil
import java.io.IOException

class VideoActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "VideoActivity"
    }

    private var binding: ActivityVideoBinding? = null
    private var mMediaMuxerWrapper: MediaMuxerWrapper? = null

    override fun onResume() {
        super.onResume()
        binding?.apply {
            cameraView.onResume()
        }
        updateRomData()
    }

    override fun onPause() {
        super.onPause()
        stopRecord()
        binding?.apply {
            cameraView.onPause()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initView()
    }

    private fun initView() {
        binding?.apply {
            titleText.text = "视频录制"
            bindInfoText.text = "卖家质检 8888888888888888888\n视频类型 卖家质检"
            videoInfoText.text = ""

            cameraView.setBestPreViewSizeListener {
                if (it == null) {
                    return@setBestPreViewSizeListener
                }
                cameraView.resizeCameraView(it)
                cameraView.reSetVideoSize()
            }
            playButton.setOnClickListener {
                if (playButton.getRecording()) {
                    stopRecord()
                } else {
                    startRecord()
                }
            }
        }
    }

    private fun updateRomData() {
        binding?.apply {
            videoInfoText.text =
                "视频大小:0MB\n可用空间:${RomUtil.getRomAvailableStr()}\n(共${RomUtil.getRomTotalSizeStr()})"
        }
    }

    private fun startRecord() {
        try {
            mMediaMuxerWrapper = MediaMuxerWrapper(".mp4")
            binding?.cameraView?.let {
                MediaVideoEncoder(
                    mMediaMuxerWrapper,
                    mMediaEncoderListener,
                    it.videoWidth,
                    it.videoHeight
                )
                MediaAudioEncoder(
                    mMediaMuxerWrapper,
                    mMediaEncoderListener
                )
            }
            mMediaMuxerWrapper?.prepare()
            mMediaMuxerWrapper?.startRecording()
            binding?.playButton?.setRecording(true)
        } catch (e: IOException) {
            e.printStackTrace()
            binding?.playButton?.setRecording(false)
        }
    }

    private fun stopRecord() {
        mMediaMuxerWrapper?.let {
            it.stopRecording()
            mMediaMuxerWrapper = null
        }
        binding?.playButton?.setRecording(false)
    }

    private val mMediaEncoderListener: MediaEncoder.MediaEncoderListener = object :
        MediaEncoder.MediaEncoderListener {
        override fun onPrepared(encoder: MediaEncoder?) {
            if (encoder is MediaVideoEncoder) {
                binding?.cameraView?.setVideoEncoder(encoder)
            }
        }

        override fun onStopped(encoder: MediaEncoder?) {
            if (encoder is MediaVideoEncoder) {
                binding?.cameraView?.setVideoEncoder(null)
            }
        }
    }

}