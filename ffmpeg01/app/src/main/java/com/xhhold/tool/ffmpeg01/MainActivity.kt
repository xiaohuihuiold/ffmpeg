package com.xhhold.tool.ffmpeg01

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            textView.text = testFFmpeg()
        }
    }
}
