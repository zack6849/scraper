package me.zcraig.threaddownloader;

import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DownloadTask extends Worker {



    DownloadTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork(){
        doStuff();
        return Result.success();
    }

    private void doStuff(){
        Data input = this.getInputData();
       String url = this.getInputData().getString("url");
       String selector = this.getInputData().getString("selector");
        try {
            if(url != null && selector != null){
                Document page = Jsoup.parse(new URL(url), 1000000);
                Elements elements = page.select(selector);
                List<String> urls = new ArrayList<String>();
                for(Element e : elements){
                    String link = "";
                    if(e.tag() == Tag.valueOf("a")){
                        link = e.attr("abs:href");
                    }
                    if(e.tag() == Tag.valueOf("img")){
                        link = e.attr("abs:src");
                    }
                    if(e.tag() == Tag.valueOf("video")){
                        link = e.selectFirst("source").attr("abs:src");
                    }
                    if(e.tag() == Tag.valueOf("audio")){
                        link = e.selectFirst("source").attr("abs:src");
                    }
                    if(link.contains("#")){
                        link = link.split("#")[0];
                    }
                    if(!link.equals("undefined") && !link.isEmpty()){
                        if(!urls.contains(link)){
                            Log.i("ThreadDownloader", "Found link: " + link);
                            urls.add(link);
                        }
                    }
                }
                for(String link : urls){
                    if(url.startsWith("http")){
                        queueDownload(link);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void queueDownload(String URL) throws MalformedURLException {
        DownloadManager downloadManager = (DownloadManager) (this.getApplicationContext()).getSystemService(Context.DOWNLOAD_SERVICE);

        Uri u = Uri.parse(URL);
        DownloadManager.Request req = new DownloadManager.Request(u);
        req.setVisibleInDownloadsUi(false).setAllowedOverRoaming(true).setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , "ThreadDownloader/" +  new URL(URL).getFile());
        long refid = downloadManager.enqueue(req);
    }
}
