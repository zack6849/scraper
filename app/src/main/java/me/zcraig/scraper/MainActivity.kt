package me.zcraig.scraper

import android.Manifest
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    private val linkTextField: EditText
        get() = (this.findViewById<View>(R.id.urlText) as EditText)

    private val selectorTextField: EditText
        get() = (this.findViewById<View>(R.id.selectorText) as EditText)

    private val linkText: String
        get() = linkTextField.text.toString()

    private val selectorText: String
        get() = selectorTextField.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        setContentView(R.layout.activity_main)

        if (selectorText.isEmpty()) {
            selectorTextField.setText("a.fileThumb");
        }

        val download = findViewById<Button>(R.id.downloadButton)
        download.setOnClickListener {
            if (linkText.isEmpty()) {
                linkTextField.selectAll();
                val clippy = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager;
                if (clippy.hasPrimaryClip()) {
                    val item = clippy.primaryClip!!.getItemAt(0);
                    val clipboardUrl = item!!.text;
                    Log.i(getString(R.string.app_name), "Clipboard URL: $clipboardUrl")
                    linkTextField.setText(clipboardUrl)
                } else {
                    Log.i(getString(R.string.app_name), "No clipboard contents :(")
                }
            }
            val data = Data.Builder().putString("url", linkText).putString("selector", selectorText).build()
            val req = OneTimeWorkRequest.Builder(DownloadTask::class.java).setInputData(data).build()
            WorkManager.getInstance().enqueue(req)
        }
    }
}
