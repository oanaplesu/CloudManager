package utils.tasks.dropbox;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class DownloadFileDropboxTask extends AsyncTask<String, Void, File>
        implements CloudRequestTask {
    private ProgressDialog mDialog;
    private DbxClientV2 mDbxClient;
    private final CloudService.DownloadFileCallback mCallback;
    private Exception mException;
    private boolean mSaveTmp;


    public DownloadFileDropboxTask(DbxClientV2 dbxClient,
                                   ProgressDialog dialog,
                                   boolean saveTmp,
                                   CloudService.DownloadFileCallback callback) {
        this.mCallback = callback;
        this.mDbxClient = dbxClient;
        this.mDialog = dialog;
        this.mSaveTmp = saveTmp;
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Downloading File");
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
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            FileMetadata fileMetadata = (FileMetadata) mDbxClient.files().getMetadata(fileId);
            mDbxClient.files().download(fileId, fileMetadata.getRev())
                    .download(outputStream);
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
