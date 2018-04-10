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

public class CreateFolderGoogleDriveTask extends AsyncTask<String, Void, Void> {
    private GoogleAccountCredential mCredential;
    private CreateFolderCallback mCallback;
    private Exception mException;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public CreateFolderGoogleDriveTask(GoogleAccountCredential credential,
                                       CreateFolderCallback callback) {
        this.mCredential = credential;
        this.mCallback = callback;
    }

    @Override
    protected Void doInBackground(String... args) {
        String accountEmail = args[0];
        String folderId = args[1].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[1];
        String newFolderName = args[2];

        mCredential.setSelectedAccountName(accountEmail);
        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), mCredential).build();

        File fileMetadata = new File();
        fileMetadata.setTitle(newFolderName);
        fileMetadata.setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE);
        fileMetadata.setParents(Collections.singletonList(
            new ParentReference().setId(folderId)));

        try {
            service.files().insert(fileMetadata)
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
