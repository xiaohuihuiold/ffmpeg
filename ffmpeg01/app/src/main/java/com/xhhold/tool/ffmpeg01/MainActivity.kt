package com.xhhold.tool.ffmpeg01

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.media.AudioTrack
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioManager
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioFormat.CHANNEL_OUT_STEREO


class MainActivity : AppCompatActivity() {

    external fun testFFmpeg()
    
    external fun playSound(input: String)

    private var audioTrack: AudioTrack? = null
    
    // 这个方法  是C进行调用
    fun createTrack(sampleRateInHz: Int, nb_channals: Int) {
        val channaleConfig: Int//通道数
        when (nb_channals) {
            1 -> channaleConfig = CHANNEL_OUT_MONO
            2 -> channaleConfig = CHANNEL_OUT_STEREO
            else -> channaleConfig = CHANNEL_OUT_MONO
        }
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRateInHz,
            channaleConfig, ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRateInHz, channaleConfig,
            ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM
        )
        audioTrack!!.play()
    }

    // C传入音频数据
    fun playTrack(buffer: ByteArray, lenth: Int) {
        if (audioTrack != null && audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.write(buffer, 0, lenth)
        }
    }

    companion object {
        init {
            System.loadLibrary("avcodec")
            System.loadLibrary("avdevice")
            System.loadLibrary("avfilter")
            System.loadLibrary("avformat")
            System.loadLibrary("avutil")
            System.loadLibrary("swresample")
            System.loadLibrary("swscale")
            System.loadLibrary("ffmmpeg01")
        }

        const val PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            testFFmpeg()
        }

        init()
    }

    private fun init() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), PERMISSION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show()
        }
    }
}
