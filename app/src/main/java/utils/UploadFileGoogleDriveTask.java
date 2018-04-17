package utils;


import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import db.AppDatabase;

public class UploadFileGoogleDriveTask extends AsyncTask<String, Void, Void> {
    private ProgressDialog mDialog;
    private Drive mService;
    private final UploadFileCallback mCallback;
    private Exception mException;
    private java.io.File mFile;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public UploadFileGoogleDriveTask(Drive service,
                                     java.io.File file, ProgressDialog dialog,
                                     UploadFileCallback callback) {
        this.mService = service;
        this.mCallback = callback;
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
        String accountEmail = args[0];
        String folderId = args[1].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[1];

        if (mFile == null) {
            return null;
        }

        String remoteFileName = mFile.getName();

        File body = new File();
        body.setTitle(remoteFileName);
        String mimeType = CloudResource.getMimeTypeFromFileName(remoteFileName);
        body.setMimeType(mimeType);
        body.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

        FileContent mediaContent = new FileContent(mimeType, mFile);

        try {
            mService.files().insert(body, mediaContent).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
            return null;
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
