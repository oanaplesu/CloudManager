package utils.tasks.google;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;


import java.io.IOException;
import java.util.Collections;

import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class UploadFileGoogleDriveTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private ProgressDialog mDialog;
    private Drive mService;
    private final CloudService.GenericCallback mCallback;
    private Exception mException;
    private java.io.File mFile;


    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public UploadFileGoogleDriveTask(Drive service,
                                     java.io.File file, ProgressDialog dialog,
                                     CloudService.GenericCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mFile = file;
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Uploading");
            mDialog.show();
        }
    }

    @Override
    protected Void doInBackground(String... args) {
        if (mFile == null) {
            return null;
        }

        String folderId = args[0].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[0];
        String fileName = args[1];

        File body = new File();
        body.setTitle(fileName);

        String mimeType = CloudResource.getMimeTypeFromFileName(fileName);
        body.setMimeType(mimeType);
        body.setParents(Collections.singletonList(new ParentReference().setId(folderId)));

        FileContent mediaContent = new FileContent(mimeType, mFile);

        try {
            mService.files().insert(body, mediaContent).execute();
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (mDialog != null && mDialog.isShowing()) {
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
