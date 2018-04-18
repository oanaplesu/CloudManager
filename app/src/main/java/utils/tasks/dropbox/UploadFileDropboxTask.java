package utils.tasks.dropbox;


import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class UploadFileDropboxTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private ProgressDialog mDialog;
    private DbxClientV2 mDbxClient;
    private final CloudService.GenericCallback mCallback;
    private Exception mException;
    private File mFile;


    public UploadFileDropboxTask(DbxClientV2 dbxClient,
                                 java.io.File file, ProgressDialog dialog,
                                 CloudService.GenericCallback callback) {
        this.mCallback = callback;
        this.mDbxClient = dbxClient;
        this.mFile = file;
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage("Uploading");
        mDialog.show();
    }

    @Override
    protected Void doInBackground(String... args) {
        String folderId = args[0];

        if (mFile == null) {
            return null;
        }

        String remoteFileName = mFile.getName();
        try (InputStream inputStream = new FileInputStream(mFile)) {
                mDbxClient.files().uploadBuilder(folderId + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete();
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
