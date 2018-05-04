package utils.tasks.google;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.dropbox.core.v2.files.FileMetadata;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class DownloadFileGoogleDriveTask extends AsyncTask<String, Void, File>
        implements CloudRequestTask {
    private Drive mService;
    private CloudService.DownloadFileCallback mCallback;
    private Exception mException;
    private ProgressDialog mDialog;
    private boolean mSaveTmp;


    public DownloadFileGoogleDriveTask(Drive service, ProgressDialog dialog,
                                       boolean saveTmp,
                                       CloudService.DownloadFileCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mDialog = dialog;
        this.mSaveTmp = saveTmp;
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Downloading file");
            mDialog.show();
        }
    }

    @Override
    protected File doInBackground(String... args) {
        String fileId = args[0];
        String fileName = args[1];

        File file;

        if(mSaveTmp) {
            try {
                String cacheDirPath = args[2];
                file = File.createTempFile(fileName, null, new File(cacheDirPath));
            } catch (IOException e) {
                mException = e;
                return null;
            }
        } else {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            file = new File(path, fileName);

            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                    return null;
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            mService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            return file;
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(result);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
