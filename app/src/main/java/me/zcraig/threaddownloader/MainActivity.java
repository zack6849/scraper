package me.zcraig.threaddownloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        setContentView(R.layout.activity_main);
        Button download = findViewById(R.id.downloadButton);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data data = new Data.Builder().putString("url", getLinkText()).putString("selector", getSelectorText()).build();
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(DownloadTask.class).setInputData(data).build();
                WorkManager.getInstance().enqueue(req);
            }
        });
    }

    public String getLinkText(){
        return ((EditText) this.findViewById(R.id.urlText)).getText().toString();
    }

    public String getSelectorText(){
        return ((EditText) this.findViewById(R.id.selectorText)).getText().toString();
    }
}
