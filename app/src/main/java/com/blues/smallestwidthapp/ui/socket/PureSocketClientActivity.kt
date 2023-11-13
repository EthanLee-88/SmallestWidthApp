package com.blues.smallestwidthapp.ui.socket

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blues.smallestwidthapp.R
import com.blues.smallestwidthapp.databinding.ActivitySocketClientBinding
import com.blues.smallestwidthapp.ui.utils.GlideEngine
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.bean.ZxingConfig
import com.yzq.zxinglibrary.common.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.Socket

class PureSocketClientActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "PureSocketClientActivity"
        private const val CODE_REQUEST = 10086
    }

    private var binding: ActivitySocketClientBinding? = null
    private var clientSocket: Socket? = null
    private var printWriter: PrintStream? = null
    private var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocketClientBinding.inflate(layoutInflater)
        binding?.let { setContentView(it.root) }
        mHandler = Handler(mainLooper)
        setViewListener()
    }

    private fun setViewListener() {
        binding?.apply {
            bindButton.setOnClickListener {
                val intent = Intent(this@PureSocketClientActivity, CaptureActivity::class.java)
                val config = ZxingConfig()
                config.isPlayBeep = true //是否播放扫描声音 默认为true
                config.isShake = true //是否震动  默认为true
                config.isDecodeBarCode = true //是否扫描条形码 默认为true
                config.reactColor = R.color.purple_200 //设置扫描框四个角的颜色 默认为白色
                config.frameLineColor = R.color.purple_500 //设置扫描框边框颜色 默认无色
                config.scanLineColor = R.color.purple_700 //设置扫描线的颜色 默认白色
                config.isFullScreenScan = false //是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config)
                startActivityForResult(intent, CODE_REQUEST)
            }
            uploadButton.setOnClickListener {
                getFile()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == CODE_REQUEST) && (resultCode == RESULT_OK)) {
            if (data != null) {
                val scanCode = data.getStringExtra(Constant.CODED_CONTENT)
                Log.d(TAG, "scanCode = $scanCode")
                scanCode?.let {
                    connectServer(it)
                }
            }
        }
    }

    private fun connectServer(ip: String) {
        lifecycleScope.launch {
            connect(ip)
        }
    }

    private suspend fun connect(ip: String) {
        withContext(Dispatchers.IO) {
            try {
                val socket = Socket(ip, 6688)
                clientSocket = socket
                printWriter = PrintStream(clientSocket!!.getOutputStream())
                val bufferReader =
                    BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
                do {
                    Log.d(TAG, "msg = read")
                    val msg: String? = bufferReader.readLine()
                    Log.d(TAG, "msg = $msg")
                    showMsg(msg)
                } while (msg != null)
            } catch (e: Exception) {
                Log.d(TAG, "connect fail = ${e.message}")
            }
        }
    }

    private fun showMsg(msg: String?) {
        msg?.let {
            binding?.msgShow?.let {
                it.post {
                    it.text = msg
                }
            }
        }
    }

    private fun getFile() {
        EasyPhotos.createAlbum(this, true, true, GlideEngine.getInstance())
            .setFileProviderAuthority("com.blues.smallestwidthapp")
            .setCount(1)
            .filter(Type.VIDEO)
            .start(object : SelectCallback() {
                override fun onResult(photos: ArrayList<Photo>, isOriginal: Boolean) {
                    if (photos.size == 0) {
                        return
                    }
                    val photo = photos[0]
                    Log.d(TAG, "photo = $photo")
                    uploadFile(photo)
                }

                override fun onCancel() {}
            })
    }

    private fun uploadFile(photo: Photo) {
        lifecycleScope.launch {
            uploadFileNew(photo)
        }
    }

    private suspend fun uploadFileNew(photo: Photo) {
        if (clientSocket == null) {
            return
        }
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            val uploadFile = File(photo.path)
            val fileLength = uploadFile.length()
            var total: Long = 0
            var len: Int
            val bytes = ByteArray(1024)
            try {
                inputStream = FileInputStream(photo.path)
                outputStream = clientSocket!!.getOutputStream()
                val dataOutputStream = DataOutputStream(outputStream)
                dataOutputStream.writeUTF(uploadFile.name)
                while (inputStream.read(bytes).also { len = it } != -1) {
                    outputStream.write(bytes, 0, len)
                    total += len.toLong()
                    val progress = (total * 100 / fileLength).toInt()
                    val msg = "$progress/100"
                    showMsg(msg)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                e.printStackTrace()
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

}