package utils;


import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;

public class CreateFolderGoogleDriveTask extends AsyncTask<String, Void, Void> {
    private Drive mService;
    private CreateFolderCallback mCallback;
    private Exception mException;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public CreateFolderGoogleDriveTask(Drive service,
                                       CreateFolderCallback callback) {
        this.mService = service;
        this.mCallback = callback;
    }

    @Override
    protected Void doInBackground(String... args) {
        String folderId = args[0].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[0];
        String newFolderName = args[1];

        File fileMetadata = new File();
        fileMetadata.setTitle(newFolderName);
        fileMetadata.setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE);
        fileMetadata.setParents(Collections.singletonList(
            new ParentReference().setId(folderId)));

        try {
            mService.files().insert(fileMetadata)
                    .setFields("id, parents")
                    .execute();
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete();
        }
    }
}
