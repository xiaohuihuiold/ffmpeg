package com.xhhold.tool.ffmpeg01

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    external fun testFFmpeg(): String

    companion object {
        init {
            System.loadLibrary("ffmmpeg01")
        }

        const val PERMISSION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            textView.text = testFFmpeg()
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
