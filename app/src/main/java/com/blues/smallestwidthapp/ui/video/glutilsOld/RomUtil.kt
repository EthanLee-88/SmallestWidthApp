package com.blues.smallestwidthapp.ui.video.glutilsOld

import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.math.BigDecimal

class RomUtil {
    companion object {
        private const val TAG = "RomUtil"
        fun getRomTotalSize(): Long {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalCount = statFs.blockCountLong
            val size = statFs.blockSizeLong
            val totalSize = totalCount * size
            Log.d(TAG, "totalCount = $totalCount + size = $size + totalSize = $totalSize")
            return totalSize
        }

        fun getRomTotalSizeStr(): String {
            val total = getRomTotalSize()
            val totalGb = total / (1000 * 1000 * 1000)
            Log.d(TAG, "totalGb = $totalGb")
            return "$totalGb" + "GB"
        }

        fun getRomAvailable(): Long {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableCount = statFs.availableBlocksLong
            val size = statFs.blockSizeLong
            val availableSize = availableCount * size
            Log.d(
                TAG,
                "availableCount = $availableCount + size = $size + availableSize = $availableSize"
            )
            return availableSize
        }

        fun getRomAvailableStr(): String {
            val availableSize = getRomAvailable()
            val available = BigDecimal(availableSize)
            val divide = BigDecimal(1000 * 1000 * 1000)
            val sizeStr =
                available.divide(divide, 2, BigDecimal.ROUND_HALF_UP).toString()
            Log.d(TAG, "sizeStr = $sizeStr")
            return sizeStr + "GB"
        }
    }
}