package utils;


import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import db.AppDatabase;

public class UploadFileDropboxTask extends AsyncTask<String, Void, Void> {
    private ProgressDialog mDialog;
    private AppDatabase mDatabase;
    private final UploadFileCallback mCallback;
    private Exception mException;
    private File mFile;


    public UploadFileDropboxTask(AppDatabase database,
                                 java.io.File file, ProgressDialog dialog,
                                 UploadFileCallback callback) {
        this.mCallback = callback;
        this.mDatabase = database;
        this.mFile = file;
        this.mDialog = dialog;
    }

    private DbxClientV2 getDbxClient(String accesToken) {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        return new DbxClientV2(requestConfig, accesToken);
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage("Uploading");
        mDialog.show();
    }

    @Override
    protected Void doInBackground(String... args) {
        String accountEmail = args[0];
        String folderId = args[1];

        String token = mDatabase.dropboxUserDao().getTokenForAccount(accountEmail);
        DbxClientV2 dbxClient = getDbxClient(token);

        if (mFile == null) {
            return null;
        }

        String remoteFileName = mFile.getName();
        try (InputStream inputStream = new FileInputStream(mFile)) {
                dbxClient.files().uploadBuilder(folderId + "/" + remoteFileName)
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
}
