package me.zcraig.scraper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup
import org.jsoup.parser.Tag
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class DownloadTask internal constructor(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        queueDownloads()
        return Result.success()
    }

    private fun queueDownloads() {
        val url = this.inputData.getString("url")
        val selector = this.inputData.getString("selector")
        try {
            if (url != null && selector != null) {
                val page = Jsoup.parse(URL(url), 1000000)
                val elements = page.select(selector)
                val urls = ArrayList<String>()
                for (e in elements) {
                    var link = ""
                    if (e.tag() === Tag.valueOf("a")) {
                        link = e.attr("abs:href")
                    }
                    if (e.tag() === Tag.valueOf("img")) {
                        link = e.attr("abs:src")
                    }
                    if (e.tag() === Tag.valueOf("video")) {
                        link = e.selectFirst("source").attr("abs:src")
                    }
                    if (e.tag() === Tag.valueOf("audio")) {
                        link = e.selectFirst("source").attr("abs:src")
                    }
                    if (link.contains("#")) {
                        link = link.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    }
                    if (link != "undefined" && !link.isEmpty()) {
                        if (!urls.contains(link)) {
                            Log.i(applicationContext.getString(R.string.app_name), "Found link: $link")
                            urls.add(link)
                        }
                    }
                }
                for (link in urls) {
                    if (url.startsWith("http")) {
                        queueDownload(link)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(MalformedURLException::class)
    private fun queueDownload(URL: String) {
        val downloadManager = this.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val u = Uri.parse(URL)
        val req = DownloadManager.Request(u)
        req.setVisibleInDownloadsUi(false).setAllowedOverRoaming(true).setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        val dest =  URL(URL).file;
        val downloadDir = applicationContext.getString(R.string.app_name)
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$downloadDir/$dest")
        Log.i(applicationContext.getString(R.string.app_name),"Downloading $URL to $dest")
        downloadManager.enqueue(req);
    }
}
