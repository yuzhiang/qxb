package io.github.yuzhiang.qxb.Service;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i("onReceive. intent:{}" + (intent != null ? intent.toUri(0) : null));
        if (intent != null) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                LogUtils.i("downloadId: " + downloadId);
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                String type = downloadManager.getMimeTypeForDownloadedFile(downloadId);
                LogUtils.i("getMimeTypeForDownloadedFile: " + type);

                Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
                LogUtils.i("UriForDownloadedFile: " + uri);

                if (uri != null) {
                    Intent openIntent = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, type)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(openIntent);
                    } catch (Exception e) {
                        LogUtils.e(e);
                    }
                }

            }
        }
    }
}
